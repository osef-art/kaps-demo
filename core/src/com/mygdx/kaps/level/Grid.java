package com.mygdx.kaps.level;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Grid {
    private static class Column {
        private final List<Optional<CapsulePart>> tiles;

        private Column(int tiles) {
            if (tiles <= 0) throw new IllegalArgumentException("Invalid column size: " + tiles);
            this.tiles = new ArrayList<>(tiles);
            IntStream.range(0, tiles).forEach(c -> this.tiles.add(Optional.empty()));
        }

        private int height() {
            return tiles.size();
        }

        private void clear(int y) {
            tiles.set(y, Optional.empty());
        }

        private void set(int y, CapsulePart caps) {
            tiles.set(y, Optional.of(caps));
        }

        private Optional<CapsulePart> get(int i) {
            return tiles.get(i);
        }
    }

    private class MatchHandler {
        private final int MINIMUM_MATCH_LENGTH;

        public MatchHandler(int length) {
            MINIMUM_MATCH_LENGTH = length;
        }

        private boolean isMatch(Set<CapsulePart> match) {
            return match.size() >= MINIMUM_MATCH_LENGTH && match.stream().map(CapsulePart::color).distinct().count() == 1;
        }

        private Set<CapsulePart> rangesFoundIn(Grid grid, BiFunction<CapsulePart, Integer, Coordinates> browsingPattern) {
            return grid.stack().stream()
              // map to a set of caps that are within range
              .map(c -> IntStream.range(0, MINIMUM_MATCH_LENGTH)
                .mapToObj(n -> get(browsingPattern.apply(c, n)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableSet())
              )
              .filter(this::isMatch)
              .flatMap(Collection::stream)
              .collect(Collectors.toUnmodifiableSet());
        }

        private Set<CapsulePart> rowsFoundIn(Grid grid) {
            return rangesFoundIn(grid, (c, n) -> c.coordinates().mapped(x -> x + n, y -> y));
        }

        private Set<CapsulePart> columnsFoundIn(Grid grid) {
            return rangesFoundIn(grid, (c, n) -> c.coordinates().mapped(x -> x, y -> y + n));
        }
    }

    private final MatchHandler matchBrowser = new MatchHandler(4);
    private final List<Column> columns;

    Grid(int columns, int rows) {
        if (columns < 2) throw new IllegalArgumentException("Insufficient grid width: " + columns);
        this.columns = new ArrayList<>(columns);
        IntStream.range(0, columns).forEach(c -> this.columns.add(new Column(rows)));
    }

    int getWidth() {
        return columns.size();
    }

    int getHeight() {
        return columns.get(0).height();
    }

    boolean isInGrid(Coordinates coordinates) {
        return isInGrid(coordinates.x, coordinates.y);
    }

    boolean isInGrid(int x, int y) {
        return 0 <= x && x < getWidth() && 0 <= y && y < getHeight();
    }

    Optional<CapsulePart> get(Coordinates coordinates) {
        return get(coordinates.x, coordinates.y);
    }

    Optional<CapsulePart> get(int x, int y) {
        return isInGrid(x, y) ? columns.get(x).get(y) : Optional.empty();
    }

    private Set<CapsulePart> stack() {
        return columns.stream()
          .flatMap(c -> c.tiles.stream())
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toUnmodifiableSet());
    }

    private void set(int x, int y, CapsulePart caps) {
        columns.get(x).set(y, caps);
    }

    void put(CapsulePart caps) {
        set(caps.coordinates().x, caps.coordinates().y, caps);
    }

    private void hit(CapsulePart part) {
        hit(part.coordinates().x, part.coordinates().y);
    }

    private void hit(int x, int y) {
        columns.get(x).clear(y);
    }

    void deleteMatches() {
        Stream.of(matchBrowser.rowsFoundIn(this), matchBrowser.columnsFoundIn(this))
          .flatMap(Collection::stream)
          .forEach(this::hit);
    }
}
