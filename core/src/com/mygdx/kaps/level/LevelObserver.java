package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.Timer;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

interface LevelObserver {
    default void onCapsuleFlipped() {}

    default void onCapsuleFreeze() {}

    default void onObjectPaint(GridObject obj, Color color) {}

    default void onTileAttack(Coordinates coordinates, AttackType type) {}

    default void onObjectHit(GridObject obj) {}

    default void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        destroyed.values().forEach(s -> s.forEach(this::onObjectHit));
    }

    default void onIllegalMove() {}

    default void onCapsuleDrop() {}

    default void onCapsuleSpawn() {}

    default void onSidekickTriggered(Sidekick triggered) {}

    default void onLevelUpdate() {}
}

class SoundPlayerObserver implements LevelObserver {
    private final SoundStream mainStream = new SoundStream(.5f);
    private final SoundStream subStream = new SoundStream(.25f);

    @Override
    public void onCapsuleFlipped() {
        mainStream.play(SoundStream.SoundStore.FLIP);
    }

    @Override
    public void onCapsuleFreeze() {
        subStream.play(SoundStream.SoundStore.LIGHT_IMPACT);
    }

    @Override
    public void onTileAttack(Coordinates coordinates, AttackType type) {
        subStream.play(type.sound());
    }

    @Override
    public void onObjectPaint(GridObject obj, Color color) {
        subStream.play(SoundStream.SoundStore.PAINT);
    }

    @Override
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        var containsGerms = destroyed.values().stream()
          .flatMap(Collection::stream)
          .anyMatch(GridObject::isGerm);
        if (containsGerms) mainStream.play(SoundStream.SoundStore.PLOP, 0);
        else if (destroyed.values().stream().anyMatch(s -> s.size() >= 5))
            mainStream.play(SoundStream.SoundStore.MATCH_FIVE);
        else if (!destroyed.isEmpty()) mainStream.play(SoundStream.SoundStore.IMPACT);
    }

    @Override
    public void onIllegalMove() {
        subStream.play(SoundStream.SoundStore.CANT);
    }

    @Override
    public void onCapsuleDrop() {
        mainStream.play(SoundStream.SoundStore.DROP);
    }

    @Override
    public void onSidekickTriggered(Sidekick triggered) {
        triggered.ifActiveElse(
          s -> mainStream.play(SoundStream.SoundStore.TRIGGER),
          s -> mainStream.play(SoundStream.SoundStore.GENERATED)
        );
    }
}

class SidekicksObserver implements LevelObserver {
    private final Map<Color, Sidekick> sidekickMap = new HashMap<>();

    SidekicksObserver(List<Sidekick> sidekicks) {
        sidekicks.forEach(s -> sidekickMap.put(s.color(), s));
    }

    @Override
    public void onObjectHit(GridObject obj) {
        if (sidekickMap.containsKey(obj.color())) sidekickMap.get(obj.color()).ifActive(ManaSidekick::increaseMana);
    }

    @Override
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        LevelObserver.super.onMatchPerformed(destroyed);
        sidekickMap.forEach((color, sdk) -> sdk.ifPassive(s -> {
            if (destroyed.containsKey(color) && destroyed.get(color).size() >= 5) s.decreaseCooldown();
        }));
    }

    @Override
    public void onCapsuleSpawn() {
        sidekickMap.values().forEach(sdk -> sdk.ifPassive(CooldownSidekick::decreaseCooldown));
    }
}

class ParticleManager implements LevelObserver {
    static class GridParticleEffect {
        private final Coordinates coordinates;
        private final AnimatedSprite anim;
        private final float scale;

        private GridParticleEffect(Coordinates coordinates, AnimatedSprite anim, float scale) {
            this.coordinates = coordinates;
            this.scale = scale;
            this.anim = anim;
        }

        private GridParticleEffect(GridObject obj) {
            this(obj.coordinates(), obj.poppingAnim(), 1);
        }

        private GridParticleEffect(GridObject obj, Color color) {
            this(obj.coordinates(), SpriteData.poppingAnimation(color), 1.5f);
        }

        private GridParticleEffect(Coordinates coordinates, AttackType type) {
            this(coordinates, SpriteData.attackEffect(type), 1.25f);
        }

        float getScale() {
            return scale;
        }

        boolean hasVanished() {
            return anim.isFinished();
        }

        Sprite getSprite() {
            return anim.getCurrentSprite();
        }

        Coordinates coordinates() {
            return coordinates;
        }

        void updateAnim() {
            anim.updateExistenceTime();
        }
    }

    static class ManaParticle {
        private final Timer progression;
        private final Coordinates coordinates;
        private final Sidekick.SidekickId target;

        private ManaParticle(GridObject obj, Sidekick target) {
            progression = Timer.ofMilliseconds(750 + new Random().nextInt(500));
            this.coordinates = obj.coordinates();
            this.target = target.id();
        }

        Sidekick.SidekickId getTarget() {
            return target;
        }

        Coordinates coordinates() {
            return coordinates;
        }

        Color color() {
            return target.color();
        }

        double ratio() {
            return progression.ratio();
        }

        boolean hasArrived() {
            return ratio() >= 1;
        }
    }

    private final List<GridParticleEffect> popping = new ArrayList<>();
    private final List<GridParticleEffect> attacks = new ArrayList<>();
    private final List<ManaParticle> mana = new ArrayList<>();
    private final List<Sidekick> sidekicks;

    ParticleManager(List<Sidekick> sidekicks) {this.sidekicks = sidekicks;}

    Stream<GridParticleEffect> getParticleEffects() {
        return Stream.of(popping, attacks).flatMap(Collection::stream);
    }

    List<ManaParticle> getManaParticles() {
        return mana;
    }

    @Override
    public void onObjectHit(GridObject obj) {
        popping.add(new GridParticleEffect(obj));
        sidekicks.stream()
          .filter(sdk -> sdk.color() == obj.color())
          .findFirst()
          .ifPresent(sdk -> sdk.ifActive(s -> mana.add(new ManaParticle(obj, s))));
    }

    @Override
    public void onTileAttack(Coordinates coordinates, AttackType type) {
        attacks.add(new GridParticleEffect(coordinates, type));
    }

    @Override
    public void onObjectPaint(GridObject obj, Color color) {
        attacks.add(new GridParticleEffect(obj, color));
    }

    @Override
    public void onLevelUpdate() {
        getParticleEffects().forEach(GridParticleEffect::updateAnim);
        popping.removeIf(GridParticleEffect::hasVanished);
        attacks.removeIf(GridParticleEffect::hasVanished);
        mana.removeIf(ManaParticle::hasArrived);
    }
}

class GameEndManager implements LevelObserver {
    private enum GameEndCase {
        GERMS_CLEARED(lvl -> lvl.getGrid().germsCount() <= 0, "LEVEL CLEARED !", SoundStream.SoundStore.CLEARED),
        SPAWN_OVERLAP(lvl -> lvl.controlledCapsules().stream()
          .map(c -> c.atLeastOneVerify(p -> lvl.getGrid().get(p.coordinates())
            .map(o -> !o.isDropping())
            .orElse(false)
          ))
          .reduce(Boolean::logicalOr)
          .orElse(false), "GAME OVER !", SoundStream.SoundStore.GAME_OVER);

        private final SoundStream.SoundStore sound;
        private final Predicate<Level> condition;
        private final String message;

        GameEndCase(Predicate<Level> condition, String message, SoundStream.SoundStore sound) {
            this.condition = condition;
            this.message = message;
            this.sound = sound;
        }

        void endGameIfChecked(Level level) {
            if (!condition.test(level)) return;
            SoundStream.play(sound, .75f);
            System.out.println(message);
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }
    }

    private final Level level;

    GameEndManager(Level level) {
        this.level = level;
    }

    @Override
    public void onLevelUpdate() {
        if (level.visualParticles().getParticleEffects().findAny().isEmpty())
            Arrays.stream(GameEndCase.values()).forEach(c -> c.endGameIfChecked(level));
    }
}
