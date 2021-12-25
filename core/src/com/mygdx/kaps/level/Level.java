package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Level {
    private final List<Gelule> gelules = new ArrayList<>();
    private final List<Timer> timers = new ArrayList<>();
    private final Set<Color> colors;
    private final Grid grid;

    // TODO: replace by sidekick set
    public Level(Set<Color> colors) {
        colors.add(Color.randomBlank());
        this.colors = colors;
        grid = new Grid(6, 15);

        var dippingTimer = Timer.ofSeconds(1, this::dipOrAcceptGelule);
        var droppingTimer = Timer.ofMilliseconds(1, this::dipAllDroppingGelules);
        timers.addAll(Arrays.asList(dippingTimer, droppingTimer));
    }

    Set<Color> getColors() {
        return colors;
    }

    List<Gelule> getGelules() {
        return gelules;
    }

    Grid getGrid() {
        return grid;
    }

    private void dipAllDroppingGelules() {
        gelules.stream()
          .filter(Gelule::isFalling)
          .forEach(g -> {
              if (g.bothVerify(c -> c.getCoordinates().y >= 1)) g.dip();
              else accept(g);
          });
    }

    private void performIfPossibleElse(Predicate<Gelule> condition, Consumer<Gelule> action,
                                       Consumer<Gelule> alternative) {
        gelules.stream()
          .filter(Predicate.not(Gelule::isFalling))
          .forEach(g -> {
              if (condition.test(g)) action.accept(g);
              else alternative.accept(g);
          });
    }

    private void performIfPossible(Predicate<Gelule> condition, Consumer<Gelule> action) {
        performIfPossibleElse(condition, action, g -> {
        });
    }

    // gelule moves
    public void dipOrAcceptGelule() {
        performIfPossibleElse(g -> g.dipped().isInGrid(grid), Gelule::dip, this::accept);
    }

    public void flipGelule() {
        performIfPossibleElse(g -> g.flipped().isInGrid(grid), Gelule::flip,
          g -> performIfPossible(f -> f.flipped().movedBack().isInGrid(grid), f -> {
              f.flip();
              f.moveBack();
          })
        );
    }

    public void moveGeluleLeft() {
        performIfPossible(g -> g.movedLeft().isInGrid(grid), Gelule::moveLeft);
    }

    public void moveGeluleRight() {
        performIfPossible(g -> g.movedRight().isInGrid(grid), Gelule::moveRight);
    }

    public void dropGelule() {
        performIfPossible(g -> true, Gelule::startFalling);
    }

    public void holdGelule() {
    }

    // update
    private void spawnGelule() {
        gelules.add(Gelule.randomNewInstance(this));
    }

    private void accept(Gelule gelule) {
        gelule.forEachCapsule(grid::put);
        gelule.freeze();
    }

    public void update() {
        gelules.removeIf(Gelule::isFrozen);
        if (gelules.stream().noneMatch(Predicate.not(Gelule::isFalling))) spawnGelule();
        timers.forEach(Timer::resetIfExceeds);
    }
}
