package com.mygdx.kaps.level;

import com.mygdx.kaps.sound.SoundStream;

public enum AttackType {
    SLICE("slice", SoundStream.SoundStore.SLICE),
    FIRE("fire", SoundStream.SoundStore.FIRE),
    FIREARM("fire", SoundStream.SoundStore.SHOT),
    MELEE("pain", SoundStream.SoundStore.SHOT),
    // MAGIC("pain", SoundStream.SoundStore.SLICE),
    BRUSH("paint", SoundStream.SoundStore.PAINT),
    ;

    private final SoundStream.SoundStore sound;
    private final String path;

    AttackType(String path, SoundStream.SoundStore sound) {
        this.sound = sound;
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }

    public SoundStream.SoundStore sound() {
        return sound;
    }
}
