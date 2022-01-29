package com.mygdx.kaps;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    private static float lerp(float from, float to, double ratio) {
        return from + (to - from) * (float) ratio;
    }

    public static float easeLerp(float from, float to, double ratio) {
        return lerp(from, to, ratio * ratio * (3f - 2f * ratio));
    }

    public static <T> T getRandomFrom(T... elems) {
        return getRandomFrom(Arrays.stream(elems));
    }

    public static <T> T getRandomFrom(Stream<? extends T> stream) {
        return getRandomFrom(stream.collect(Collectors.toUnmodifiableList()));
    }

    public static <T> T getRandomFrom(Collection<? extends T> collection) {
        if (collection.isEmpty())
            throw new IllegalArgumentException("Can't get random element from empty collection.");
        return new ArrayList<>(collection).get(new Random().nextInt(collection.size()));
    }

    public static <T> Optional<T> getOptionalRandomFrom(Stream<? extends T> stream) {
        return getOptionalRandomFrom(stream.collect(Collectors.toUnmodifiableList()));
    }

    public static <T> Optional<T> getOptionalRandomFrom(Collection<? extends T> collection) {
        if (collection.isEmpty()) return Optional.empty();
        return Optional.of(getRandomFrom(collection));
    }
}
