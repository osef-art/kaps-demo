package com.mygdx.kaps.level;

import com.mygdx.kaps.level.gridobject.*;

import java.util.*;
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

    static class Match {
        private static final int BIG_SIZE = 5;
        private static final int HUGE_SIZE = 9;
        private final Set<GridObject> objects;
        private final Color color;

        private Match(Set<GridObject> matched) {
            objects = Objects.requireNonNull(matched);
            color = matched.stream().findAny().map(GridObject::color).orElseThrow(
              () -> new IllegalArgumentException("Match can't be empty.")
            );
        }

        private Match(Stream<GridObject> stream) {
            this(stream.collect(Collectors.toUnmodifiableSet()));
        }

        @Override
        public String toString() {
            return "(" + objects.size() + ")" + objects;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Match)) return false;
            return ((Match) o).objects.equals(objects);
        }

        @Override
        public int hashCode() {
            return Objects.hash(objects);
        }

        Color color() {
            return color;
        }

        boolean isBig() {
            return objects.size() >= 5;
        }

        private boolean isMatch(MatchHandler.MatchPattern pattern) {
            return objects.size() >= pattern.relativeTiles.size() && objects.stream().map(GridObject::color).distinct().count() == 1;
        }

        private boolean canMergeWith(Match match) {
            return objects.stream().anyMatch(match.objects::contains);
        }

        private Match mergedWith(Match match) {
            return canMergeWith(match) ?
                     new Match(Stream.of(objects, match.objects).flatMap(Collection::stream)) : this;
        }

        <T> T dependingOnSize(T classic, T big, T huge) {
            return objects.size() >= HUGE_SIZE ? huge : objects.size() >= BIG_SIZE ? big : classic;
        }

        Stream<? extends GridObject> stream() {
            return objects.stream();
        }

        Stream<Germ> getGerms() {
            return stream().filter(GridObject::isGerm).map(o -> (Germ) o);
        }
    }

    private static class MatchHandler {
        private static class MatchPattern {
            private final Set<Function<Coordinates, Coordinates>> relativeTiles;
            private static final MatchPattern SQUARE_PATTERN = new MatchPattern(
              c -> c.addedTo(-1, -1), c -> c.addedTo(-1, 0), c -> c.addedTo(-1, 1),
              c -> c.addedTo(0, 1), c -> c.addedTo(1, 1), c -> c.addedTo(1, 0),
              c -> c.addedTo(1, -1), c -> c.addedTo(0, -1)
            );
            private static final MatchPattern COLUMN_PATTERN = new MatchPattern(
              c -> c.addedTo(0, -1),
              c -> c.addedTo(0, -2),
              c -> c.addedTo(0, -3)
            );
            private static final MatchPattern ROW_PATTERN = new MatchPattern(
              c -> c.addedTo(1, 0),
              c -> c.addedTo(2, 0),
              c -> c.addedTo(3, 0)
            );

            @SafeVarargs
            private MatchPattern(Function<Coordinates, Coordinates>... tiles) {
                relativeTiles = Arrays.stream(tiles).collect(Collectors.toSet());
                relativeTiles.add(Function.identity());
            }
        }

        private static final List<MatchPattern> patterns = Arrays.asList(
          MatchPattern.SQUARE_PATTERN, MatchPattern.COLUMN_PATTERN, MatchPattern.ROW_PATTERN
        );

        private Set<Match> matchesFoundIn(Grid grid, MatchPattern pattern) {
            return grid.stack()
              .filter(Predicate.not(GridObject::isDropping))
              // map each object to a set of matches that follows pattern
              .map(o -> new Match(pattern.relativeTiles.stream()
                .map(p -> p.apply(o.coordinates()))
                .map(grid::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
              ))
              .filter(m -> m.isMatch(pattern))
              .collect(Collectors.toUnmodifiableSet());
        }

        private Set<Match> mergedMatches(Set<Match> matches) {
            return Stream.of(
                // matches that doesn't merge with any other match
                matches.stream().filter(m -> matches.stream()
                  .filter(Predicate.not(m::equals))
                  .noneMatch(m::canMergeWith)),
                // merged matches
                matches.stream().flatMap(m1 -> matches.stream()
                  .filter(Predicate.not(m1::equals))
                  .filter(m1::canMergeWith)
                  .map(m1::mergedWith)
                )).flatMap(Function.identity())
              .collect(Collectors.toUnmodifiableSet());
        }

        private Set<Match> allMatchesFoundIn(Grid grid) {
            return mergedMatches(patterns.stream()
              .map(p -> matchesFoundIn(grid, p))
              .flatMap(Collection::stream)
              .collect(Collectors.toUnmodifiableSet()));
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
        everyTile().forEach(c -> get(c).ifPresent(o -> o.coordinates().set(c)));
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

    boolean canBePut(GridObject obj) {
        return isInGridBounds(obj.coordinates()) && get(obj.coordinates()).isEmpty();
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
        detach(old.coordinates());
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

    // stack operations
    Set<Match> getMatches() {
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
}
