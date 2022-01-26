package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.*;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.Timer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

interface LevelObserver {
    default void onCapsuleFlipped() {}

    default void onCapsuleFreeze() {}

    default void onCapsuleDrop() {}

    default void onIllegalMove() {}

    default void onObjectHit(GridObject obj) {}

    default void onObjectDestroyed(GridObject obj) {
        onObjectHit(obj);
    }

    default void onObjectPaint(GridObject obj, Color color) {}

    default void onTileAttack(Coordinates coordinates, AttackType type) {}

    default void onMatchPerformed(Map<Color, Set<? extends GridObject>> matches) {}

    default void onSidekickTriggered(Sidekick triggered) {}

    default void onGermTriggered(CooldownGerm triggered) {}

    default void onGamePaused() {}

    default void onGameResumed() {}

    default void onLevelUpdate(Level level) {}
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
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> matches) {
        var germs = matches.values().stream()
          .flatMap(Collection::stream)
          .filter(GridObject::isGerm)
          .map(o -> (Germ) o)
          .collect(Collectors.toUnmodifiableSet());

        if (germs.size() > 0) {
            if (germs.stream().anyMatch(GridObject::isDestroyed))
                mainStream.play(SoundStream.SoundStore.PLOP, 0);
            else if (germs.stream().anyMatch(g -> g.isOfKind(Germ.GermKind.WALL)))
                mainStream.play(SoundStream.SoundStore.BREAK);
        } else if (matches.values().stream().anyMatch(s -> s.size() >= 5))
            mainStream.play(SoundStream.SoundStore.MATCH_FIVE);
        else if (!matches.isEmpty()) mainStream.play(SoundStream.SoundStore.IMPACT);
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

    @Override
    public void onGermTriggered(CooldownGerm triggered) {
        mainStream.play(triggered.attackSound());
    }
}

class ParticleManager implements LevelObserver {
    static class GridParticleEffect {
        private final Coordinates coordinates;
        private final AnimatedSprite anim;
        private final Runnable finalJob;
        private final float scale;

        private GridParticleEffect(Coordinates coordinates, AnimatedSprite anim, Runnable job, float scale) {
            this.coordinates = coordinates;
            this.scale = scale;
            this.anim = anim;
            finalJob = job;
        }

        private GridParticleEffect(GridObject obj) {
            this(obj.coordinates(), obj.poppingAnim(), () -> {}, 1);
        }

        private GridParticleEffect(GridObject obj, Color color) {
            this(obj.coordinates(), SpriteData.poppingAnimation(color), () -> {}, 1.5f);
        }

        private GridParticleEffect(Coordinates coordinates, AttackType type) {
            this(coordinates, SpriteData.attackEffect(type), () -> {}, 1.25f);
        }

        private GridParticleEffect(CooldownGerm germ) {
            this(germ.coordinates(), SpriteData.attackEffect(germ), germ::stopAttacking, 1);
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
            IntStream.range(0, obj.manaWorth()).forEach(
              n -> mana.add(new ManaParticle(obj, sidekicks.get(obj.color())))
            );
    }

    void addContaminationEffect(CooldownGerm germ) {
        popping.add(new GridParticleEffect(germ));
    }

    @Override
    public void onObjectHit(GridObject obj) {
        popping.add(new GridParticleEffect(obj));
    }

    @Override
    public void onObjectDestroyed(GridObject obj) {
        LevelObserver.super.onObjectDestroyed(obj);
        addManaParticle(obj);
    }

    @Override
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> matches) {
        matches.forEach((color, match) -> {
            if (!sidekicks.containsKey(color)) return;
            int bonus = sidekicks.get(color).ifActiveElse(
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
    public void onGamePaused() {
        mana.forEach(p -> p.progression.pause());
    }

    @Override
    public void onGameResumed() {
        mana.forEach(p -> p.progression.resume());
    }

    @Override
    public void onLevelUpdate(Level level) {
        getParticleEffects().forEach(GridParticleEffect::updateAnim);
        mana.stream().filter(ManaParticle::hasArrived).forEach(m -> {
            sidekicks.get(m.target.color).ifActiveElse(ManaSidekick::increaseMana, CooldownSidekick::decreaseCooldown);
            stream.play(SoundStream.SoundStore.MANA);
        });
        popping.stream().filter(GridParticleEffect::hasVanished).forEach(p -> p.finalJob.run());
        popping.removeIf(GridParticleEffect::hasVanished);
        attacks.removeIf(GridParticleEffect::hasVanished);
        mana.removeIf(ManaParticle::hasArrived);
    }
}

class GameEndManager implements LevelObserver {
    enum GameEndCase {
        GERMS_CLEARED(lvl -> lvl.getGermsCount() <= 0, "LEVEL CLEARED !", SoundStream.SoundStore.CLEARED),
        SPAWN_OVERLAP(lvl -> lvl.controlledCapsules().stream()
          .map(c -> c.atLeastOneVerify(p -> lvl.getGrid().get(p.coordinates())
            .map(o -> !o.isDropping())
            .orElse(false)
          ))
          .reduce(Boolean::logicalOr)
          .orElse(false), "GAME OVER !", SoundStream.SoundStore.GAME_OVER),
        ;

        private final SoundStream.SoundStore sound;
        private final Predicate<Level> condition;
        private final String message;

        GameEndCase(Predicate<Level> condition, String message, SoundStream.SoundStore sound) {
            this.condition = condition;
            this.message = message;
            this.sound = sound;
        }

        private void endGameIfChecked(Level level) {
            if (!condition.test(level)) return;
            SoundStream.play(sound, .75f);
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }

        public String getMessage() {
            return message;
        }
    }

    void ifChecked(Level level, Consumer<GameEndCase> action) {
        Arrays.stream(GameEndCase.values())
          .filter(c -> c.condition.test(level))
          .forEach(action);
    }

    @Override
    public void onLevelUpdate(Level level) {
        if (level.visualParticles().getParticleEffects().findAny().isEmpty())
            Arrays.stream(GameEndCase.values()).forEach(c -> c.endGameIfChecked(level));
    }
}

class LevelAttackObserver implements LevelObserver {
    private final Level level;

    LevelAttackObserver(Level level) {
        this.level = level;
    }

    @Override
    public void onCapsuleFreeze() {
        level.triggerGermsIfReady();
        level.triggerSidekicksIfReady();
    }
}
