package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.Germ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LevelLoader {
    private final static LinkedList<Integer> LEVEL_SEQ = new LinkedList<>();
    private final static Set<SidekickId> sidekicks = new HashSet<>();
    private final static int MAX_SIDEKICKS = 2;
    private final static int RANDOM_GRID = -1;
    private final static int MAX_LEVELS = 20;

    private void fillParty() {
        while (sidekicks.size() < MAX_SIDEKICKS)
            sidekicks.add(Utils.getRandomFrom(SidekickId.values()));
    }

    private Level generateRandomGrid(int width, int height, int germNumber) {
        if (germNumber > width * Math.min(height, 3))
            throw new IllegalArgumentException(String.format("Too many germs for a %dx%d grid: %d", width, height, germNumber));

        var grid = new Grid(width, height);

        do {
            var germ = Germ.random(new Coordinates(
              new Random().nextInt(grid.getWidth()),
              new Random().nextInt(3)
            ));
            if (grid.canBePut(germ)) {
                grid.put(germ);
                germNumber--;
            }
        } while (germNumber > 0);

        fillParty();
        return new Level("??? - Bonus", grid, sidekicks);
    }

    private Level loadLevelFromNumber(int lvl) {
        String filePath = "android/assets/levels/level" + lvl;
        List<String> gridData;

        try {
            Stream<String> lines = Files.lines(Path.of(filePath));
            gridData = lines.collect(Collectors.toList());
            lines.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("An error occurred while parsing file %s: %s", filePath, e));
        }

        var rows = gridData.stream().map(line -> {
            var chars = line.toCharArray();
            var germs = IntStream.range(0, chars.length)
              .mapToObj(n -> chars[n])
              .map(c -> c == '.' ? null : Germ.ofSymbol(c))
              .collect(Collectors.toList());
            return new Grid.Row(germs);
        }).collect(Collectors.toList());

        fillParty();
        return new Level("1 - " + lvl, new Grid(rows), sidekicks);
    }

    public void addSidekick(String name) {
        if (sidekicks.size() < MAX_SIDEKICKS)
            sidekicks.add(SidekickId.ofName(name));
    }

    public void addRandomGrid() {
        addLevel(RANDOM_GRID);
    }

    public void addRandomLevel() {
        addLevel(new Random().nextInt(MAX_LEVELS + 1));
    }

    public void addLevel(int lvl) {
        if (lvl < -1 || MAX_LEVELS < lvl)
            throw new IllegalArgumentException(String.format(
              "Invalid level number: %d (%s)", lvl, lvl < MAX_LEVELS ? "max level is 20" : "negative value")
            );
        LEVEL_SEQ.add(lvl);
    }

    public Optional<Level> takeNextLevel() {
        if (LEVEL_SEQ.isEmpty()) return Optional.empty();

        return Optional.of(LEVEL_SEQ.removeFirst())
          .map(lvl -> lvl == RANDOM_GRID ? generateRandomGrid(
              6 + new Random().nextInt(3),
              10 + new Random().nextInt(3),
              10 + new Random().nextInt(3)
            ) : loadLevelFromNumber(lvl)
          );
    }
}
