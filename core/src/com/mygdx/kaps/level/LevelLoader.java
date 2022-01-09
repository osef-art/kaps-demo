package com.mygdx.kaps.level;

import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.Germ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        var grid = new Grid(width, height);
        var sidekicks = Sidekick.randomSet(2);
        var blank = Color.randomBlank();

        do {
            var randomTile = new Coordinates(
              new Random().nextInt(grid.getWidth()),
              new Random().nextInt(3)
            );
            if (grid.isEmptyTile(randomTile)) {
                grid.put(Germ.random(randomTile, Color.random(Color.getSetFrom(sidekicks, blank))));
                germNumber--;
            }
        } while (germNumber > 0);

        var lvl = new Level(grid, sidekicks, blank);
        return isValid(lvl) ? lvl : randomLevel(width, height, germNumber);
    }

    public static Level loadFrom(String filePath) {
        List<String> gridData;
        var sidekicks = Sidekick.randomSet(2);
        var blankColor = Color.randomBlank();

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
              .map(c -> c == '.' ? null : Germ.ofSymbol(c, Color.getSetFrom(sidekicks, blankColor)))
              .collect(Collectors.toList());
            return new Grid.Row(germs);
        }).collect(Collectors.toList());

        var lvl = new Level(new Grid(rows), sidekicks, blankColor);
        return isValid(lvl) ? lvl : loadFrom(filePath);
    }
}
