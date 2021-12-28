package com.mygdx.kaps.level;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Grid {
    private static class Row {
        private final List<Optional<GridObject>> tiles;

        private Row(int tiles) {
            if (tiles < 2) throw new IllegalArgumentException("Invalid row length: " + tiles);
            this.tiles = new ArrayList<>(tiles);
            IntStream.range(0, tiles).forEach(c -> this.tiles.add(Optional.empty()));
        }

        private int width() {
            return tiles.size();
        }

        private void clear(int n) {
            tiles.set(n, Optional.empty());
        }

        private void set(int n, GridObject obj) {
            tiles.set(n, Optional.of(obj));
        }

        private Optional<GridObject> get(int n) {
            return tiles.get(n);
        }
    }

    private class MatchHandler {
        private final int MINIMUM_MATCH_LENGTH;

        public MatchHandler(int length) {
            MINIMUM_MATCH_LENGTH = length;
        }

        private boolean isMatch(Set<GridObject> match) {
            return match.size() >= MINIMUM_MATCH_LENGTH && match.stream().map(GridObject::color).distinct().count() == 1;
        }

        private Set<GridObject> rangesFoundIn(Grid grid, BiFunction<GridObject, Integer, Coordinates> browsingPattern) {
            return grid.stack().stream()
              // map to a set of objects that are within range
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

        private Set<GridObject> rowsFoundIn(Grid grid) {
            return rangesFoundIn(grid, (c, n) -> c.coordinates().mapped(x -> x + n, y -> y));
        }

        private Set<GridObject> columnsFoundIn(Grid grid) {
            return rangesFoundIn(grid, (c, n) -> c.coordinates().mapped(x -> x, y -> y + n));
        }
    }

    private final MatchHandler matchBrowser = new MatchHandler(4);
    private final List<Row> rows;

    Grid(int columns, int rows) {
        if (rows < 2) throw new IllegalArgumentException("Insufficient grid width: " + rows);
        this.rows = new ArrayList<>(rows);
        IntStream.range(0, rows).forEach(c -> this.rows.add(new Row(columns)));
    }

    int getWidth() {
        return rows.get(0).width();
    }

    int getHeight() {
        return rows.size();
    }

    private boolean isInGridBounds(int x, int y) {
        return 0 <= x && x < getWidth() && 0 <= y && y < getHeight();
    }

    boolean isInGridBounds(Coordinates coordinates) {
        return isInGridBounds(coordinates.x, coordinates.y);
    }

    boolean isEmptyTile(Coordinates coordinates) {
        return isInGridBounds(coordinates) && get(coordinates).isEmpty();
    }

    Optional<GridObject> get(Coordinates coordinates) {
        return get(coordinates.x, coordinates.y);
    }

    Optional<GridObject> get(int x, int y) {
        return isInGridBounds(x, y) ? rows.get(y).get(x) : Optional.empty();
    }

    private Set<GridObject> stack() {
        return rows.stream()
          .flatMap(c -> c.tiles.stream())
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toUnmodifiableSet());
    }

    long germsCount() {
        return stack().stream().filter(IGridObject::isGerm).count();
    }

    private void set(Coordinates coordinates, GridObject obj) {
        rows.get(coordinates.y).set(coordinates.x, obj);
        obj.coordinates().set(coordinates);
    }

    void put(GridObject obj) {
        set(obj.coordinates(), obj);
    }

    private void clear(Coordinates coordinates) {
        rows.get(coordinates.y).clear(coordinates.x);
    }

    private void hit(Coordinates coordinates) {
        detach(coordinates);
        clear(coordinates);
    }

    private void detach(Coordinates coordinates) {
        get(coordinates).ifPresent(o -> {
            if (o.isCapsule()) ((CapsulePart) o).linked().ifPresent(l -> put(new CapsulePart(l)));
        });
    }

    private void swap(Coordinates c1, Coordinates c2) {
        var tmp = get(c1);
        get(c2).ifPresentOrElse(o2 -> set(c1, o2), () -> clear(c1));
        tmp.ifPresentOrElse(o1 -> set(c2, o1), () -> clear(c2));
    }

    private boolean containsMatches() {
        return Stream.of(matchBrowser.rowsFoundIn(this), matchBrowser.columnsFoundIn(this))
          .mapToLong(Collection::size)
          .sum() > 0;
    }

    private void deleteMatches() {
        Stream.of(matchBrowser.rowsFoundIn(this), matchBrowser.columnsFoundIn(this))
          .flatMap(Collection::stream)
          .forEach(c -> hit(c.coordinates()));
    }

    void deleteMatchesRecursively() {
        do {
            deleteMatches();
            dropEveryCapsule();
        } while (containsMatches());
    }

    void dropEveryCapsule() {
        stack().stream()
          .filter(IGridObject::isCapsule)
          .map(o -> (CapsulePart) o)
          .map(c -> {
              Predicate<CapsulePart> condition = p -> isEmptyTile(p.coordinates().addedTo(0, -1));
              var verified = c.orientation().isVertical() ? c.atLeastOneVerify(condition) : c.verify(condition);
              if (verified) {
                  c.applyToBoth(p -> swap(p.coordinates(), p.coordinates().addedTo(0, -1)));
                  return true;
              }
              return false;
          })
          .reduce(Boolean::logicalOr)
          .ifPresent(couldDip -> {
              if (couldDip) dropEveryCapsule();
          });
    }
}
