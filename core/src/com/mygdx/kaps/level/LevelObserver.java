package com.mygdx.kaps.level;

import com.mygdx.kaps.sound.SoundStream;

import java.util.Set;

interface LevelObserver {
    void onCapsuleFlipped();

    void onCapsuleFreeze();

    void onMatchPerformed(Set<? extends GridObject> destroyed);

    void onIllegalMove();

    void onCapsuleDrop();
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
        else subStream.play(SoundStream.SoundStore.IMPACT);
    }

    @Override
    public void onIllegalMove() {
        mainStream.play(SoundStream.SoundStore.CANT);
    }

    @Override
    public void onCapsuleDrop() {
        mainStream.play(SoundStream.SoundStore.DROP);
    }
}
