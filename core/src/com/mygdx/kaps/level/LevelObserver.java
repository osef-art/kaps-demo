package com.mygdx.kaps.level;

import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.sound.SoundStream;

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
}

class SidekicksObserver implements LevelObserver {
    private final Level level;
    private final HashMap<Color, Sidekick> sidekickMap = new HashMap<>();

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
            sidekick.triggerIfReady(level);
        });
    }
}
