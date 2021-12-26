package com.mygdx.kaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static <T> T getRandomFrom(Stream<? extends T> stream) {
        return getRandomFrom(stream.collect(Collectors.toList()));
    }

    public static <T> T getRandomFrom(Collection<? extends T> collection) {
        if (collection.size() == 0)
            throw new IllegalArgumentException("Can't get random from empty collection.");
        return new ArrayList<>(collection).get(new Random().nextInt(collection.size()));
    }
}
