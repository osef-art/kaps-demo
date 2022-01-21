package com.mygdx.kaps;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static float lerp(float from, float to, double ratio) {
        float easeRatio = (float) (ratio * ratio * (3f - 2f * ratio));
        return from + (to - from) * easeRatio;
    }

    public static <T> T getRandomFrom(T... elems) {
        return getRandomFrom(Arrays.stream(elems));
    }

    public static <T> T getRandomFrom(Stream<? extends T> stream) {
        return getRandomFrom(stream.collect(Collectors.toUnmodifiableList()));
    }

    public static <T> T getRandomFrom(Collection<? extends T> collection) {
        if (collection.size() == 0)
            throw new IllegalArgumentException("Can't get random from empty collection.");
        return new ArrayList<>(collection).get(new Random().nextInt(collection.size()));
    }

    public static <T> Optional<T> getOptionalRandomFrom(Stream<? extends T> stream) {
        return getOptionalRandomFrom(stream.collect(Collectors.toUnmodifiableList()));
    }

    public static <T> Optional<T> getOptionalRandomFrom(Collection<? extends T> collection) {
        if (collection.isEmpty())
            return Optional.empty();
        return Optional.of(getRandomFrom(collection));
    }
}
