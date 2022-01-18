package com.mygdx.kaps.level;

import com.mygdx.kaps.level.gridobject.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Grid {
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
              .filter(Predicate.not(GridObject::isDropping))
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
        forEachTile((x, y) -> get(x, y).ifPresent(o -> o.coordinates().set(x, y)));
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

    public boolean isInGridBounds(Coordinates coordinates) {
        return isInGridBounds(coordinates.x, coordinates.y);
    }

    boolean isEmptyTile(Coordinates coordinates) {
        return isInGridBounds(coordinates) && get(coordinates).isEmpty();
    }

    public Optional<? extends GridObject> get(Coordinates coordinates) {
        return get(coordinates.x, coordinates.y);
    }

    Optional<? extends GridObject> get(int x, int y) {
        return isInGridBounds(x, y) ? rows.get(y).get(x) : Optional.empty();
    }

    Set<? extends GridObject> stack() {
        return rows.stream()
          .flatMap(Row::stream)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toUnmodifiableSet());
    }

    Stream<CapsulePart> capsuleStack() {
        return stack().stream()
          .filter(GridObject::isCapsule)
          .map(o -> (CapsulePart) o)
          .filter(Predicate.not(CapsulePart::isDropping));
    }

    Stream<Germ> germStack() {
        return stack().stream()
          .filter(GridObject::isGerm)
          .map(o -> (Germ) o);
    }

    long germsCount() {
        return stack().stream().filter(GridObject::isGerm).count();
    }

    // tiles operations
    void forEachTile(BiConsumer<Integer, Integer> action) {
        IntStream.range(0, getWidth()).forEach(x -> IntStream.range(0, getHeight()).forEach(y -> action.accept(x, y)));
    }

    private void set(Coordinates coordinates, GridObject obj) {
        rows.get(coordinates.y).set(coordinates.x, obj);
    }

    void put(GridObject obj) {
        set(obj.coordinates(), obj);
    }

    private void clear(Coordinates coordinates) {
        detach(coordinates);
        rows.get(coordinates.y).clear(coordinates.x);
    }

    Optional<? extends GridObject> hit(Coordinates coordinates, int damage) {
        var obj = get(coordinates);
        IntStream.range(0, damage).forEach(n -> obj.ifPresent(o -> {
            o.takeHit();
            if (o.isDestroyed()) clear(coordinates);
        }));
        return obj;
    }

    Optional<? extends GridObject> hit(GridObject o, int damage) {
        return hit(o.coordinates(), damage);
    }

    Optional<? extends GridObject> hit(GridObject o) {
        return hit(o, 1);
    }

    private void detach(Coordinates coordinates) {
        get(coordinates)
          .filter(GridObject::isCapsule)
          .map(o -> (CapsulePart) o)
          .flatMap(CapsulePart::linked)
          .ifPresent(l -> put(new CapsulePart(l)));
    }

    void repaint(GridObject obj, Color color) {
        get(obj.coordinates()).ifPresent(o -> o.repaint(color));
    }

    // stack operations
    boolean containsMatches() {
        return matchBrowser.allMatchesFoundIn(this).size() > 0;
    }

    Map<Color, Set<? extends GridObject>> hitMatches() {
        return matchBrowser.allMatchesFoundIn(this).stream()
          .map(this::hit)
          .map(Optional::get)
          .collect(Collectors.toUnmodifiableMap(GridObject::color, o -> Stream.of(o).collect(Collectors.toSet()),
            (s1, s2) -> Stream.of(s1, s2).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()))
          );
    }

    void initEveryCapsuleDropping() {
        var couldDrop = capsuleStack()
          .filter(Predicate.not(CapsulePart::isDropping))
          .filter(c -> c.verticalVerify(
            p -> p.dipped().canStandIn(this) || get(p.dipped().coordinates()).map(GridObject::isDropping).orElse(true))
          )
          .peek(CapsulePart::initDropping)
          .count() > 0;
        if (couldDrop) initEveryCapsuleDropping();
    }

    boolean dipOrFreezeDroppingCapsules() {
        return stack().stream()
          .filter(GridObject::isCapsule)
          .map(o -> (CapsulePart) o)
          .filter(CapsulePart::isDropping)
          .sorted(Comparator.comparingInt(p -> p.coordinates().y))
          .map(c -> {
              if (c.isDropping()) if (c.verticalVerify(p -> p.dipped().canStandIn(this))) {
                  c.applyToBoth(p -> clear(p.coordinates()));
                  c.applyToBoth(p -> {
                      p.dip();
                      put(p);
                  });
                  c.linked().ifPresent(CapsulePart::freeze);
              } else {
                  c.applyToBoth(CapsulePart::freeze);
                  return true;
              }
              return false;
          })
          .reduce(Boolean::logicalOr)
          .orElse(false);
    }
}
