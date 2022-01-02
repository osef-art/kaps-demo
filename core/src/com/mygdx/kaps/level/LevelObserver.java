package com.mygdx.kaps.level;

import com.mygdx.kaps.sound.SoundStream;

interface LevelObserver {
    void onCapsuleFlipped();

    void onCapsuleFreeze();

    void onMatchDeleted();

    void onIllegalMove();

    void onCapsuleDrop();
}

class SoundPlayerObserver implements LevelObserver {
    private final SoundStream stream = new SoundStream();

    @Override
    public void onCapsuleFlipped() {
        stream.play(SoundStream.SoundStore.FLIP);
    }

    @Override
    public void onCapsuleFreeze() {
        stream.play(SoundStream.SoundStore.IMPACT);
    }

    @Override
    public void onMatchDeleted() {
        stream.play(SoundStream.SoundStore.LIGHT_IMPACT);
    }

    @Override
    public void onIllegalMove() {
        stream.play(SoundStream.SoundStore.CANT);
    }

    @Override
    public void onCapsuleDrop() {
        stream.play(SoundStream.SoundStore.DROP);
    }
}
