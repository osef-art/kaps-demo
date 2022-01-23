package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.Timer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

interface LevelObserver {
    default void onCapsuleFlipped() {}

    default void onCapsuleFreeze() {}

    default void onCapsuleDrop() {}

    default void onCapsuleAccepted() {}

    default void onIllegalMove() {}

    default void onObjectPaint(GridObject obj, Color color) {}

    default void onTileAttack(Coordinates coordinates, AttackType type) {}

    default void onObjectHit(GridObject obj) {}

    default void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        destroyed.values().forEach(s -> s.forEach(this::onObjectHit));
    }

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
        mainStream.play(type.sound());
    }

    @Override
    public void onObjectPaint(GridObject obj, Color color) {
        mainStream.play(SoundStream.SoundStore.PAINT);
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
        mainStream.play(
          triggered.ifActiveElse(SoundStream.SoundStore.TRIGGER, SoundStream.SoundStore.GENERATED)
        );
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
        private final SidekickId target;

        private ManaParticle(GridObject obj, Sidekick target) {
            progression = Timer.ofMilliseconds(750 + new Random().nextInt(500));
            this.coordinates = obj.coordinates();
            this.target = target.id();
        }

        SidekickId getTarget() {
            return target;
        }

        Coordinates coordinates() {
            return coordinates;
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
    private final Map<Color, Sidekick> sidekicks;
    private final SoundStream stream = new SoundStream(.2f);

    ParticleManager(List<Sidekick> sidekicks) {
        this.sidekicks = sidekicks.stream().collect(
          Collectors.toUnmodifiableMap(Sidekick::color, Function.identity())
        );
    }

    Stream<GridParticleEffect> getParticleEffects() {
        return Stream.of(popping, attacks).flatMap(Collection::stream);
    }

    List<ManaParticle> getManaParticles() {
        return mana;
    }

    private void addManaParticle(GridObject obj) {
        if (sidekicks.containsKey(obj.color()))
            mana.add(new ManaParticle(obj, sidekicks.get(obj.color())));
    }

    @Override
    public void onObjectHit(GridObject obj) {
        popping.add(new GridParticleEffect(obj));
    }

    @Override
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        LevelObserver.super.onMatchPerformed(destroyed);
        destroyed.forEach((color, match) -> {
            if (!sidekicks.containsKey(color)) return;
            var sdk = sidekicks.get(color);
            sdk.ifActive(s -> match.forEach(this::addManaParticle));
            int bonus = sdk.ifActiveElse(
              match.size() >= 9 ? 3 : match.size() >= 5 ? 1 : 0,
              match.size() >= 9 ? 2 : match.size() >= 5 ? 1 : 0
            );
            IntStream.range(0, bonus).forEach(
              n -> addManaParticle(Utils.getRandomFrom(match))
            );
        });
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
        mana.stream().filter(ManaParticle::hasArrived).forEach(m -> {
            sidekicks.get(m.target.color).ifActiveElse(ManaSidekick::increaseMana, CooldownSidekick::decreaseCooldown);
            stream.play(SoundStream.SoundStore.MANA);
        });
        popping.removeIf(GridParticleEffect::hasVanished);
        attacks.removeIf(GridParticleEffect::hasVanished);
        mana.removeIf(ManaParticle::hasArrived);
    }
}

class GameEndManager implements LevelObserver {
    private enum GameEndCase {
        GERMS_CLEARED(lvl -> lvl.getGermsCount() <= 0, "LEVEL CLEARED !", SoundStream.SoundStore.CLEARED),
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
