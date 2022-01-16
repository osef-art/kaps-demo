package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.sound.SoundStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

interface LevelObserver {
    void onCapsuleFlipped();

    void onCapsuleFreeze();

    void onMatchPerformed(Set<? extends GridObject> destroyed);

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
    public void onMatchPerformed(Set<? extends GridObject> destroyed) {
        var containsGerms = destroyed.stream().anyMatch(GridObject::isGerm);
        if (containsGerms) mainStream.play(SoundStream.SoundStore.PLOP, 0);
        else if (destroyed.size() >= 5) mainStream.play(SoundStream.SoundStore.MATCH_FIVE);
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
    public void onCapsuleSpawn() {
    }

    @Override
    public void onLevelUpdate() {

    }
}

class SidekicksObserver implements LevelObserver {
    private final Level level;
    private final HashMap<Color, Sidekick> sidekickMap = new HashMap<>();
    private final SoundStream stream = new SoundStream(.5f);

    SidekicksObserver(Level level, List<Sidekick> sidekicks) {
        this.level = level;
        sidekicks.forEach(s -> sidekickMap.put(s.color(), s));
    }

    @Override
    public void onCapsuleFlipped() {
    }

    @Override
    public void onCapsuleFreeze() {
    }

    @Override
    public void onMatchPerformed(Set<? extends GridObject> destroyed) {
        destroyed.forEach(o -> {
            if (sidekickMap.containsKey(o.color())) sidekickMap.get(o.color()).increaseMana();
        });
    }

    @Override
    public void onIllegalMove() {
    }

    @Override
    public void onCapsuleDrop() {
    }

    @Override
    public void onCapsuleSpawn() {
        sidekickMap.values().forEach(sidekick -> {
            sidekick.decreaseCooldown();
            if (sidekick.isReady()) stream.play(sidekick.sound());
            sidekick.triggerIfReady(level);
        });
    }

    @Override
    public void onLevelUpdate() {

    }
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

    @Override
    public void onCapsuleFlipped() {
    }

    @Override
    public void onCapsuleFreeze() {
    }

    @Override
    public void onMatchPerformed(Set<? extends GridObject> destroyed) {
        destroyed.forEach(o -> popping.add(new GridParticleEffect(o)));
    }

    @Override
    public void onIllegalMove() {
    }

    @Override
    public void onCapsuleDrop() {
    }

    @Override
    public void onCapsuleSpawn() {
    }

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
