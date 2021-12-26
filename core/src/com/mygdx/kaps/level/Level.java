package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Level {
    private final List<FullCapsule> fallingCapsules = new ArrayList<>();
    private final List<Timer> timers = new ArrayList<>();
    private final Set<Color> colors;
    private final Grid grid;

    // TODO: replace by sidekick set
    public Level(Set<Color> colors) {
        colors.add(Color.randomBlank());
        this.colors = colors;
        grid = new Grid(6, 15);

        var dippingTimer = Timer.ofSeconds(1, this::dipOrAcceptCapsule);
        var droppingTimer = Timer.ofMilliseconds(1, this::dipOrAcceptDroppingCapsule);
        timers.addAll(Arrays.asList(dippingTimer, droppingTimer));
    }

    Set<Color> getColors() {
        return colors;
    }

    List<FullCapsule> fallingCapsules() {
        return fallingCapsules;
    }

    Grid getGrid() {
        return grid;
    }

    Coordinates spawnCoordinates() {
        return new Coordinates(getGrid().getWidth() / 2 - 1, getGrid().getHeight() - 1);
    }

    /**
     * Executes on each level's capsules matching {@param selection} the code conveyed by {@param action}
     * if {@param condition} is true, else executes {@param alternative} instead
     *
     * @param selection   the capsules on which act
     * @param condition   the predicate to match to execute {@param action}
     * @param action      the action to execute on each falling Capsule
     * @param alternative the action to execute on each falling Capsule if {@param condition} is not matched
     */
    private void performIfPossible(Predicate<FullCapsule> selection, Predicate<FullCapsule> condition,
                                   Consumer<FullCapsule> action, Consumer<FullCapsule> alternative) {
        fallingCapsules.stream()
          .filter(selection)
          .forEach(c -> {
              if (condition.test(c)) action.accept(c);
              else alternative.accept(c);
          });
    }

    /**
     * Executes on each level's capsules matching {@param selection} the code conveyed by {@param action}
     * if  {@param condition} is true
     *
     * @param selection the capsules on which act
     * @param condition the predicate to match to execute {@param action}
     * @param action    the action to execute on each falling Capsule
     */
    private void performIfPossible(Predicate<FullCapsule> selection, Predicate<FullCapsule> condition, Consumer<FullCapsule> action) {
        performIfPossible(selection, condition, action, c -> {
        });
    }

    // capsule moves
    public void moveCapsuleLeft() {
        performIfPossible(Predicate.not(FullCapsule::isFalling), c -> c.movedLeft().canStandIn(grid), FullCapsule::moveLeft);
    }

    public void moveCapsuleRight() {
        performIfPossible(Predicate.not(FullCapsule::isFalling), c -> c.movedRight().canStandIn(grid), FullCapsule::moveRight);
    }

    public void dipOrAcceptCapsule() {
        performIfPossible(Predicate.not(FullCapsule::isFalling), c -> c.dipped().canStandIn(grid), FullCapsule::dip, this::accept);
    }

    private void dipOrAcceptDroppingCapsule() {
        performIfPossible(FullCapsule::isFalling, c -> c.dipped().canStandIn(grid), FullCapsule::dip, this::accept);
    }

    public void flipCapsule() {
        performIfPossible(Predicate.not(FullCapsule::isFalling), c -> c.flipped().canStandIn(grid), FullCapsule::flip,
          g -> performIfPossible(f -> true, f -> f.flipped().movedBack().canStandIn(grid), f -> {
              f.flip();
              f.moveForward();
          })
        );
    }

    public void dropCapsule() {
        performIfPossible(Predicate.not(FullCapsule::isFalling), c -> true, FullCapsule::startFalling);
    }

    public void holdCapsule() {
    }

    // update
    private void spawnCapsule() {
        fallingCapsules.add(FullCapsule.randomNewInstance(this));
        fallingCapsules.forEach(g -> {
            if (!g.canStandIn(grid)) System.exit(0);
        });
    }

    private void accept(FullCapsule fullCapsule) {
        fullCapsule.forEachCapsule(grid::put);
        fullCapsule.freeze();
        grid.deleteMatches();
    }

    public void update() {
        fallingCapsules.removeIf(FullCapsule::isFrozen);
        if (fallingCapsules.stream().noneMatch(Predicate.not(FullCapsule::isFalling))) spawnCapsule();
        timers.forEach(Timer::resetIfExceeds);
    }
}
