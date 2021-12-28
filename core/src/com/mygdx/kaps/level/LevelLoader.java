package com.mygdx.kaps.level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LevelLoader {
    private static boolean isValid(Level level) {
        return !level.getGrid().containsMatches();
    }

    public static Level randomLevel(int width, int height, int germNumber) {
        if (germNumber > width * Math.min(height, 3))
            throw new IllegalArgumentException("Too many germs for a " + width + "x" + height + " grid: " + germNumber);

        var colors = new HashSet<Color>();
        var grid = new Grid(width, height);
        while (colors.size() < 3)
            colors.add(Color.randomNonBlank());

        do {
            var randomTile = new Coordinates(
              new Random().nextInt(grid.getWidth()),
              new Random().nextInt(3)
            );
            if (grid.isEmptyTile(randomTile)) {
                grid.put(new Germ(randomTile, Color.random(colors)));
                germNumber--;
            }
        } while (germNumber > 0);

        var lvl =  new Level(grid, colors);
        return isValid(lvl) ? lvl : randomLevel(width, height, germNumber);
    }

    public static Level loadFrom(String filePath) {
        List<String> gridData;
        var colors = new HashSet<Color>();
        while (colors.size() < 3)
            colors.add(Color.randomNonBlank());

        try {
            Stream<String> lines = Files.lines(Path.of(filePath));
            gridData = lines.collect(Collectors.toList());
            lines.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("An error occurred while parsing file " + filePath + ": " + e);
        }

        var rows = gridData.stream().map(line -> {
            var chars = line.toCharArray();
            var germs = IntStream.range(0, chars.length)
              .mapToObj(n -> chars[n])
              .map(c -> c == '.' ? null : Germ.ofSymbol(c, colors))
              .collect(Collectors.toList());
            return new Grid.Row(germs);
        }).collect(Collectors.toList());

        var lvl = new Level(new Grid(rows), colors);
        return isValid(lvl) ? lvl : loadFrom(filePath);
    }
}
