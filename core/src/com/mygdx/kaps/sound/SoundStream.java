package com.mygdx.kaps.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.mygdx.kaps.Utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SoundStream {
    public enum SoundStore {
        LIGHT_IMPACT,
        SLICE(2),
        PAINT(3),
        FIRE(2),
        FLIP(3),
        IMPACT,
        DROP,
        CANT,
        ;

        private final Set<String> paths;

        SoundStore() {
            this(1);
        }

        SoundStore(int set) {
            if (0 <= set) throw new IllegalArgumentException("Invalid set number.");
            paths = IntStream.range(0, set)
              .mapToObj(n -> "android/assets/sounds/" + this + n + ".wav")
              .collect(Collectors.toSet());
        }

        private String getRandomPath() {
            return Utils.getRandomFrom(paths);
        }
    }

    private Sound sound;

    public void play(SoundStore sound) {
        play(sound.getRandomPath());
    }

    public void play(String path) {
        if (sound != null) sound.dispose();
        sound = Gdx.audio.newSound(Gdx.files.internal(path));
        sound.setVolume(sound.play(), 0.015f);
    }
}
