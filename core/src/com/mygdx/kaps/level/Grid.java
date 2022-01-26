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
            return grid.stack()
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

        private Map<Color, Set<? extends GridObject>> allMatchesFoundIn(Grid grid) {
            return patterns.stream()
              .map(p -> matchesFoundIn(grid, p))
              .flatMap(Collection::stream)
              .collect(Collectors.toUnmodifiableMap(
                GridObject::color,
                o -> Stream.of(o).collect(Collectors.toSet()),
                (s1, s2) -> Stream.of(s1, s2).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()))
              );
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

    boolean isInGridBounds(Coordinates coordinates) {
        return isInGridBounds(coordinates.x, coordinates.y);
    }

    boolean isEmptyTile(Coordinates coordinates) {
        return isInGridBounds(coordinates) && get(coordinates).isEmpty();
    }

    Optional<? extends GridObject> get(int x, int y) {
        return isInGridBounds(x, y) ? rows.get(y).get(x) : Optional.empty();
    }

    Optional<? extends GridObject> get(Coordinates coordinates) {
        return get(coordinates.x, coordinates.y);
    }

    Stream<Coordinates> everyTile() {
        return IntStream.range(0, getWidth())
          .mapToObj(x -> IntStream.range(0, getHeight())
            .mapToObj(y -> new Coordinates(x, y))
          )
          .flatMap(Function.identity());
    }

    Stream<? extends GridObject> stack() {
        return rows.stream()
          .flatMap(Row::stream)
          .filter(Optional::isPresent)
          .map(Optional::get);
    }

    Stream<CapsulePart> capsuleStack() {
        return stack()
          .filter(GridObject::isCapsule)
          .map(o -> (CapsulePart) o)
          .filter(Predicate.not(CapsulePart::isDropping));
    }

    Stream<Germ> germStack() {
        return stack().filter(GridObject::isGerm).map(o -> (Germ) o);
    }

    Stream<CooldownGerm> cooldownGermStack() {
        return germStack().filter(Germ::hasCooldown).map(g -> (CooldownGerm) g);
    }

    // tiles operations
    private void set(Coordinates coordinates, GridObject obj) {
        rows.get(coordinates.y).set(coordinates.x, obj);
    }

    private void clear(Coordinates coordinates) {
        detach(coordinates);
        rows.get(coordinates.y).clear(coordinates.x);
    }

    void put(GridObject obj) {
        set(obj.coordinates(), obj);
    }

    void replace(GridObject old, GridObject newObj) {
        newObj.coordinates().set(old.coordinates());
        set(old.coordinates(), newObj);
    }

    Optional<? extends GridObject> hit(Coordinates coordinates, int damage) {
        return get(coordinates).stream()
          .peek(o -> IntStream.range(0, damage).forEach(n -> {
              o.takeHit();
              if (o.isDestroyed()) clear(coordinates);
          }))
          .findFirst();
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

    void forEachTile(BiConsumer<Integer, Integer> action) {
        everyTile().forEach(c -> action.accept(c.x, c.y));
    }

    // stack operations
    Map<Color, Set<? extends GridObject>> getMatches() {
        return matchBrowser.allMatchesFoundIn(this);
    }

    void initEveryCapsuleDropping() {
        capsuleStack()
          .filter(c -> c.verticalVerify(p -> get(p.dipped().coordinates())
            .map(GridObject::isDropping)
            .orElse(isInGridBounds(p.dipped().coordinates())))
          )
          .peek(CapsulePart::initDropping)
          .findFirst()
          .ifPresent(p -> initEveryCapsuleDropping());
    }

    boolean dipOrFreezeDroppingCapsules() {
        return stack()
          .filter(GridObject::isDropping)
          .filter(GridObject::isCapsule)
          .map(o -> (CapsulePart) o)
          .sorted(Comparator.comparingInt(p -> p.coordinates().y))
          .map(c -> {
              if (!c.isDropping()) return false;
              if (c.verticalVerify(p -> canBePut(p.dipped()))) {
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

    boolean canBePut(CapsulePart caps) {
        return isInGridBounds(caps.coordinates()) && get(caps.coordinates()).isEmpty();
    }
}
