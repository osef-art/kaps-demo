package com.mygdx.kaps.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.mygdx.kaps.Utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SoundStream {
    public enum SoundStore {
        LIGHT_IMPACT,
        SLICE(2),
        PAINT(3),
        FIRE(2),
        FLIP(3),
        PLOP(4),
        IMPACT,
        DROP,
        CANT,
        ;

        private final List<String> paths;

        SoundStore() {
            this(1);
        }

        SoundStore(int set) {
            if (set <= 0) throw new IllegalArgumentException("Invalid set number.");
            paths = IntStream.range(0, set)
              .mapToObj(n -> toString() + (set > 1 ? n : ""))
              .map(name -> "android/assets/sounds/" + name + ".wav")
              .collect(Collectors.toUnmodifiableList());
        }

        private String getRandomPath() {
            return Utils.getRandomFrom(paths);
        }

        private String getPathOfInstance(int instance) {
            return paths.get(instance);
        }
    }

    private final float volume;
    private Sound sound;

    public SoundStream() {
        this(1);
    }

    public SoundStream(float volume) {
        this.volume = volume;
    }


    public void play(SoundStore sound) {
        play(sound.getRandomPath());
    }

    public void play(SoundStore sound, int instance) {
        play(sound.getPathOfInstance(instance));
    }

    public void play(String path) {
        if (sound != null) sound.dispose();
        sound = Gdx.audio.newSound(Gdx.files.internal(path));
        sound.setVolume(sound.play(), volume);
    }
}
