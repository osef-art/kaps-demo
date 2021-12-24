package com.mygdx.kaps.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class SoundStream {
    public enum SoundRecord {
        WHOOSH(3),
        PAINT(3),
        SLICE(2),
        DASH(3),
        FIRE(2),
        HIT(6),
        ;

        private final String name;
        private final int instances;

        SoundRecord(int n) {
            this.name = toString().toLowerCase();
            if (n < 1) throw new IllegalArgumentException("Can't record a file with least than 1 instance (" + this.name + ".wav)");
            instances = n;
        }

        private static Optional<String> randomInstanceOf(String name) {
            return Arrays.stream(values())
                     .filter(s -> s.name.equals(Objects.requireNonNull(name)))
                     .map(s -> name + (s.instances > 1 ? new Random().nextInt(s.instances): ""))
                     .findFirst();
        }

        public static String getPathOf(String name) {
            return SoundRecord.randomInstanceOf(name)
                     .map(n -> "android/assets/sounds/" + n + ".wav")
                     .orElseThrow(() -> new IllegalArgumentException("Inexistant sound file: " + name + ".wav"));
        }
    }
    private Sound sound;

    public void play(SoundRecord sound) {
        play(sound.name);
    }

    public void play(String name) {
        if (sound != null) sound.dispose();
        sound = Gdx.audio.newSound(
          Gdx.files.internal(SoundRecord.getPathOf(name))
        );
        sound.setVolume(sound.play(), 0.015f);
    }
}
