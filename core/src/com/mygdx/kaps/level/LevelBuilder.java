package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.Germ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LevelBuilder {
    private final Set<Sidekick> sidekicks = new HashSet<>();
    private final Color blank = Color.randomBlank();
    private final static int maxSidekicks = 2;
    private final static int maxLevels = 20;
    private int levelNum = -1;

    private boolean isValid(Level level) {
        return !level.getGrid().containsMatches();
    }

    private void fillParty() {
        while (sidekicks.size() < maxSidekicks)
            sidekicks.add(Sidekick.random());
    }

    private Level generateRandomLevel(int width, int height, int germNumber) {
        if (germNumber > width * Math.min(height, 3))
            throw new IllegalArgumentException("Too many germs for a " + width + "x" + height + " grid: " + germNumber);

        var grid = new Grid(width, height);
        fillParty();

        do {
            var randomTile = new Coordinates(
              new Random().nextInt(grid.getWidth()),
              new Random().nextInt(3)
            );
            if (grid.isEmptyTile(randomTile)) {
                grid.put(Germ.random(randomTile, Utils.getRandomFrom(Color.getSetFrom(sidekicks, blank))));
                germNumber--;
            }
        } while (germNumber > 0);

        var lvl = new Level(grid, sidekicks, blank);
        return isValid(lvl) ? lvl : generateRandomLevel(width, height, germNumber);
    }

    private Level loadLevelFrom(String filePath) {
        List<String> gridData;
        fillParty();

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
              .map(c -> c == '.' ? null : Germ.ofSymbol(c, Color.getSetFrom(sidekicks, blank)))
              .collect(Collectors.toList());
            return new Grid.Row(germs);
        }).collect(Collectors.toList());

        var lvl = new Level(new Grid(rows), sidekicks, blank);
        return isValid(lvl) ? lvl : loadLevelFrom(filePath);
    }

    public void addSidekick(Sidekick sdk) {
        if (sidekicks.size() < maxSidekicks) sidekicks.add(sdk);
    }

    public void setRandomLevel() {
        setLevel(new Random().nextInt(maxLevels + 1));
    }

    public void setLevel(int lvl) {
        levelNum = lvl;
    }

    public Level build() {
        return levelNum <= -1 ?
                 generateRandomLevel(6, 12, 10) :
                 loadLevelFrom("android/assets/levels/level" + levelNum);
    }
}
