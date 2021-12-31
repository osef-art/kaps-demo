package com.mygdx.kaps.level;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Grid {
    static class Row {
        private final List<Optional<? extends GridObject>> tiles;

        Row(List<? extends GridObject> elems) {
            if (elems.isEmpty()) throw new IllegalArgumentException("Rows can't be empty.");
            tiles = elems.stream()
              .map(Optional::ofNullable)
              .collect(Collectors.toList());
        }

        Row(int length) {
            this(IntStream.range(0, length).mapToObj(n -> (GridObject) null).collect(Collectors.toList()));
        }

        private int width() {
            return tiles.size();
        }

        private Optional<? extends GridObject> get(int n) {
            return tiles.get(n);
        }

        private Stream<Optional<? extends GridObject>> stream() {
            return tiles.stream();
        }

        private void clear(int n) {
            tiles.set(n, Optional.empty());
        }

        private void set(int n, GridObject obj) {
            tiles.set(n, Optional.of(obj));
        }
    }

    private class MatchHandler {
        private class MatchPattern {
            private final Set<Function<Coordinates, Coordinates>> relativeTiles;

            @SafeVarargs
            private MatchPattern(Function<Coordinates, Coordinates>... tiles) {
                relativeTiles = Arrays.stream(tiles).collect(Collectors.toSet());
                relativeTiles.add(Function.identity());
            }

            private boolean isMatch(Set<? extends GridObject> match) {
                return match.size() >= relativeTiles.size() && match.stream().map(GridObject::color).distinct().count() == 1;
            }
        }

        private final List<MatchPattern> patterns;

        public MatchHandler() {
            var rowPattern = new MatchPattern(
              c -> c.addedTo(1, 0),
              c -> c.addedTo(2, 0),
              c -> c.addedTo(3, 0)
            );
            var columnPattern = new MatchPattern(
              c -> c.addedTo(0, -1),
              c -> c.addedTo(0, -2),
              c -> c.addedTo(0, -3)
            );
            var squarePattern = new MatchPattern(
              c -> c.addedTo(-1, -1),
              c -> c.addedTo(-1, 0),
              c -> c.addedTo(-1, 1),
              c -> c.addedTo(0, 1),
              c -> c.addedTo(1, 1),
              c -> c.addedTo(1, 0),
              c -> c.addedTo(1, -1),
              c -> c.addedTo(0, -1)
            );
            patterns = Arrays.asList(rowPattern, columnPattern, squarePattern);
        }

        private Set<? extends GridObject> matchesFoundIn(Grid grid, MatchPattern pattern) {
            return grid.stack().stream()
              // map each object to a set of objects that follows pattern
              .map(o -> pattern.relativeTiles.stream()
                .map(c -> c.apply(o.coordinates()))
                .map(Grid.this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet())
              )
              .filter(pattern::isMatch)
              .flatMap(Collection::stream)
              .collect(Collectors.toUnmodifiableSet());
        }

        private Set<? extends GridObject> allMatchesFoundIn(Grid grid) {
            return patterns.stream()
              .map(p -> matchesFoundIn(grid, p))
              .flatMap(Collection::stream)
              .collect(Collectors.toSet());
        }
    }

    private final MatchHandler matchBrowser = new MatchHandler();
    private final List<Row> rows;

    Grid(int columns, int rows) {
        this(IntStream.range(0, rows)
          .mapToObj(c -> new Row(columns))
          .collect(Collectors.toList()));
    }

    Grid(List<Row> rows) {
        if (rows.size() < 2)
            throw new IllegalArgumentException("Insufficient grid height: " + rows);
        if (rows.get(0).width() < 2)
            throw new IllegalArgumentException("Insufficient grid width: " + rows.get(0).width());
        if (rows.stream().map(Row::width).distinct().count() > 1)
            throw new IllegalArgumentException("Grid rows must all have same size");

        Collections.reverse(rows);
        this.rows = rows;
        IntStream.range(0, getWidth()).forEach(
          x -> IntStream.range(0, getHeight()).forEach(
            y -> get(x, y).ifPresent(o -> o.coordinates().set(x, y))
          )
        );
    }

    // getters
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

    Optional<? extends GridObject> get(Coordinates coordinates) {
        return get(coordinates.x, coordinates.y);
    }

    Optional<? extends GridObject> get(int x, int y) {
        return isInGridBounds(x, y) ? rows.get(y).get(x) : Optional.empty();
    }

    private Set<? extends GridObject> stack() {
        return rows.stream()
          .flatMap(Row::stream)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toUnmodifiableSet());
    }

    long germsCount() {
        return stack().stream().filter(IGridObject::isGerm).count();
    }

    // tiles operations
    private void set(Coordinates coordinates, GridObject obj) {
        rows.get(coordinates.y).set(coordinates.x, obj);
//        obj.coordinates().set(coordinates);
    }

    void put(GridObject obj) {
        set(obj.coordinates(), obj);
    }

    private void clear(Coordinates coordinates) {
        detach(coordinates);
        rows.get(coordinates.y).clear(coordinates.x);
    }

    private void hit(Coordinates coordinates) {
        get(coordinates).ifPresent(o -> {
            o.takeHit();
            if (o.isDestroyed()) clear(coordinates);
        });
    }

    private void hit(GridObject o) {
        hit(o.coordinates());
    }

    private void detach(Coordinates coordinates) {
        get(coordinates).ifPresent(o -> {
            if (o.isCapsule()) ((CapsulePart) o).linked().ifPresent(l -> put(new CapsulePart(l)));
        });
    }

    // stack operations
    boolean containsMatches() {
        return matchBrowser.allMatchesFoundIn(this).size() > 0;
    }

    private void deleteMatches() {
        matchBrowser.allMatchesFoundIn(this).forEach(this::hit);
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
              Predicate<CapsulePart> hasEmptyTileBelow = p -> isEmptyTile(p.coordinates().addedTo(0, -1));
              var canDip = c.orientation().isVertical() ?
                             c.atLeastOneVerify(hasEmptyTileBelow) :
                             c.verify(hasEmptyTileBelow);
              if (canDip) {
                  c.applyToBoth(p -> clear(p.coordinates()));
                  c.applyToBoth(p -> {
                      p.coordinates().add(0, -1);
                      put(p);
                  });
              }
              return canDip;
          })
          .reduce(Boolean::logicalOr)
          .ifPresent(couldDip -> {
              if (couldDip) dropEveryCapsule();
          });
    }
}
