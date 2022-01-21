package com.mygdx.kaps.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.mygdx.kaps.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SoundStream {
    public enum SoundStore {
        LIGHT_IMPACT,
        MATCH_FIVE,
        GAME_OVER,
        GENERATED,
        SLICE(2),
        TRIGGER,
        CLEARED,
        SHOT(3),
        FIRE(2),
        FLIP(3),
        PLOP(4),
        IMPACT,
        PAINT,
        PAUSE,
        DROP,
        CANT,
        MANA;

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

    private final Map<String, Sound> sounds = new HashMap<>();
    private final float volume;

    public SoundStream(float volume) {
        this.volume = volume / 5;
    }

    public static void play(SoundStore sound, float volume) {
        new SoundStream(volume).play(sound);
    }

    public void play(SoundStore sound) {
        play(sound.getRandomPath());
    }

    public void play(SoundStore sound, int instance) {
        play(sound.getPathOfInstance(instance));
    }

    private void play(String path) {
        if (sounds.containsKey(path)) sounds.get(path).dispose();
        sounds.put(path, Gdx.audio.newSound(Gdx.files.internal(path)));
        sounds.get(path).setVolume(sounds.get(path).play(), volume);
    }
}
