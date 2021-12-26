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
        var droppingTimer = Timer.ofMilliseconds(1, this::dipOrAcceptDroppingGelule);
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

    /**
     * Executes on each level's gelules matching {@param selection} the code conveyed by {@param action}
     * if {@param condition} is true, else executes {@param alternative} instead
     *
     * @param selection   the gelules on which act
     * @param condition   the predicate to match to execute {@param action}
     * @param action      the action to execute on each falling gelule
     * @param alternative the action to execute on each falling gelule if {@param condition} is not matched
     */
    private void performIfPossible(Predicate<Gelule> selection, Predicate<Gelule> condition,
                                   Consumer<Gelule> action, Consumer<Gelule> alternative) {
        gelules.stream()
          .filter(selection)
          .forEach(g -> {
              if (condition.test(g)) action.accept(g);
              else alternative.accept(g);
          });
    }

    /**
     * Executes on each level's gelules matching {@param selection} the code conveyed by {@param action}
     * if  {@param condition} is true
     *
     * @param selection the gelules on which act
     * @param condition the predicate to match to execute {@param action}
     * @param action    the action to execute on each falling gelule
     */
    private void performIfPossible(Predicate<Gelule> selection, Predicate<Gelule> condition, Consumer<Gelule> action) {
        performIfPossible(selection, condition, action, g -> {
        });
    }

    // gelule moves
    public void moveGeluleLeft() {
        performIfPossible(Predicate.not(Gelule::isFalling), g -> g.movedLeft().canStandIn(grid), Gelule::moveLeft);
    }

    public void moveGeluleRight() {
        performIfPossible(Predicate.not(Gelule::isFalling), g -> g.movedRight().canStandIn(grid), Gelule::moveRight);
    }

    public void dipOrAcceptGelule() {
        performIfPossible(Predicate.not(Gelule::isFalling), g -> g.dipped().canStandIn(grid), Gelule::dip, this::accept);
    }

    private void dipOrAcceptDroppingGelule() {
        performIfPossible(Gelule::isFalling, g -> g.dipped().canStandIn(grid), Gelule::dip, this::accept);
    }

    public void flipGelule() {
        performIfPossible(Predicate.not(Gelule::isFalling), g -> g.flipped().canStandIn(grid), Gelule::flip,
          g -> performIfPossible(f -> true, f -> f.flipped().movedBack().canStandIn(grid), f -> {
              f.flip();
              f.moveBack();
          })
        );
    }

    public void dropGelule() {
        performIfPossible(Predicate.not(Gelule::isFalling), g -> true, Gelule::startFalling);
    }

    public void holdGelule() {
    }

    // update
    private void spawnGelule() {
        gelules.add(Gelule.randomNewInstance(this));
        gelules.forEach(g -> {
            if (!g.canStandIn(grid)) System.exit(0);
        });
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
