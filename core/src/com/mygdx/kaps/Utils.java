package com.mygdx.kaps;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
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
        if (collection.size() == 0)
            return Optional.empty();
        return Optional.of(getRandomFrom(collection));
    }

    public static <T> Set<T> getRandomSetOf(Stream<? extends T> stream, int size) {
        return getRandomSetOf(stream.collect(Collectors.toUnmodifiableList()), size);
    }

    public static <T> Set<T> getRandomSetOf(Collection<? extends T> collection, int size) {
        List<T> lst = new ArrayList<>(collection);
        Collections.shuffle(lst);
        return (size >= lst.size() ? lst : lst.subList(0, size)).stream().collect(Collectors.toUnmodifiableSet());
    }
}
