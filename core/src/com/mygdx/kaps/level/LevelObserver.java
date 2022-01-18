package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;
import com.mygdx.kaps.sound.SoundStream;

import java.util.*;
import java.util.function.Predicate;

interface LevelObserver {
    void onCapsuleFlipped();

    void onCapsuleFreeze();

    void onObjectHit(GridObject obj);

    default void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        destroyed.values().forEach(s -> s.forEach(this::onObjectHit));
    }

    void onIllegalMove();

    void onCapsuleDrop();

    void onCapsuleSpawn();

    void onLevelUpdate();
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
    public void onObjectHit(GridObject obj) {}

    @Override
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        var containsGerms = destroyed.values().stream()
          .flatMap(Collection::stream)
          .anyMatch(GridObject::isGerm);
        if (containsGerms) mainStream.play(SoundStream.SoundStore.PLOP, 0);
        else if (destroyed.values().stream().anyMatch(s -> s.size() >= 5))
            mainStream.play(SoundStream.SoundStore.MATCH_FIVE);
        else mainStream.play(SoundStream.SoundStore.IMPACT);
    }

    @Override
    public void onIllegalMove() {
        mainStream.play(SoundStream.SoundStore.CANT);
    }

    @Override
    public void onCapsuleDrop() {
        mainStream.play(SoundStream.SoundStore.DROP);
    }

    @Override
    public void onCapsuleSpawn() {}

    @Override
    public void onLevelUpdate() {}
}

class SidekicksObserver implements LevelObserver {
    private final Level level;
    private final HashMap<Color, Sidekick> sidekickMap = new HashMap<>();
    private final SoundStream stream = new SoundStream(.45f);

    SidekicksObserver(Level level, List<Sidekick> sidekicks) {
        this.level = level;
        sidekicks.forEach(s -> sidekickMap.put(s.color(), s));
    }

    @Override
    public void onCapsuleFlipped() {}

    @Override
    public void onCapsuleFreeze() {}

    @Override
    public void onObjectHit(GridObject obj) {
        if (sidekickMap.containsKey(obj.color())) sidekickMap.get(obj.color()).ifActive(ManaSidekick::increaseMana);
    }

    @Override
    public void onMatchPerformed(Map<Color, Set<? extends GridObject>> destroyed) {
        LevelObserver.super.onMatchPerformed(destroyed);
        sidekickMap.forEach((color, sdk) -> sdk.ifPassive(s -> {
            if (destroyed.get(color).size() >= 5) s.decreaseCooldown();
        }));
    }

    @Override
    public void onIllegalMove() {}

    @Override
    public void onCapsuleDrop() {}

    @Override
    public void onCapsuleSpawn() {
        sidekickMap.values().forEach(sidekick -> {
            sidekick.ifPassive(CooldownSidekick::decreaseCooldown);
            if (sidekick.isReady()) {
                sidekick.ifActiveElse(
                  s -> stream.play(sidekick.sound()),
                  s -> stream.play(SoundStream.SoundStore.TRIGGER)
                );
                sidekick.trigger(level);
            }
        });
    }

    @Override
    public void onLevelUpdate() {}
}

class ParticleManager implements LevelObserver {
    interface Particle {
        boolean hasVanished();

        Sprite getSprite();

        Coordinates coordinates();

        void updateAnim();
    }

    private static class GridParticleEffect implements Particle {
        private final Coordinates coordinates;
        private final AnimatedSprite anim;

        private GridParticleEffect(GridObject obj) {
            coordinates = obj.coordinates();
            anim = obj.poppingAnim();
        }

        public GridParticleEffect(Sidekick.AttackType type, Coordinates coordinates) {
            this.coordinates = coordinates;
            anim = SpriteData.attackEffect(type);
        }

        @Override
        public boolean hasVanished() {
            return anim.isFinished();
        }

        public Sprite getSprite() {
            return anim.getCurrentSprite();
        }

        public Coordinates coordinates() {
            return coordinates;
        }

        @Override
        public void updateAnim() {
            anim.updateExistenceTime();
        }
    }

    private final List<Particle> popping = new ArrayList<>();

    List<Particle> getPoppingObjects() {
        return popping;
    }

    public void addEffect(Sidekick.AttackType type, Coordinates coordinates) {
        popping.add(new GridParticleEffect(type, coordinates));
    }

    @Override
    public void onCapsuleFlipped() {}

    @Override
    public void onCapsuleFreeze() {}

    @Override
    public void onObjectHit(GridObject obj) {
        popping.add(new GridParticleEffect(obj));
    }

    @Override
    public void onIllegalMove() {}

    @Override
    public void onCapsuleDrop() {}

    @Override
    public void onCapsuleSpawn() {}

    @Override
    public void onLevelUpdate() {
        updateAnimations();
        filterVanished();
    }

    private void updateAnimations() {
        popping.forEach(Particle::updateAnim);
    }

    private void filterVanished() {
        popping.removeIf(Particle::hasVanished);
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

        void endGameIfChecked(Level level, SoundStream stream) {
            if (!condition.test(level)) return;
            stream.play(sound);
            System.out.println(message);
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }
    }

    private final SoundStream stream = new SoundStream(.75f);
    private final Level level;

    GameEndManager(Level level) {
        this.level = level;
    }

    @Override
    public void onCapsuleFlipped() {}

    @Override
    public void onCapsuleFreeze() {}

    @Override
    public void onObjectHit(GridObject obj) {}

    @Override
    public void onIllegalMove() {}

    @Override
    public void onCapsuleDrop() {}

    @Override
    public void onCapsuleSpawn() {}

    @Override
    public void onLevelUpdate() {
        if (level.visualParticles().isEmpty())
            Arrays.stream(GameEndCase.values()).forEach(c -> c.endGameIfChecked(level, stream));
    }
}
