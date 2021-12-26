package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Level {
    private final List<Capsule> fallingCapsules = new ArrayList<>();
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

    List<Capsule> fallingCapsules() {
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
    private void performIfPossible(Predicate<Capsule> selection, Predicate<Capsule> condition,
                                   Consumer<Capsule> action, Consumer<Capsule> alternative) {
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
    private void performIfPossible(Predicate<Capsule> selection, Predicate<Capsule> condition, Consumer<Capsule> action) {
        performIfPossible(selection, condition, action, c -> {
        });
    }

    // capsule moves
    public void moveCapsuleLeft() {
        performIfPossible(Predicate.not(Capsule::isFalling), c -> c.movedLeft().canStandIn(grid), Capsule::moveLeft);
    }

    public void moveCapsuleRight() {
        performIfPossible(Predicate.not(Capsule::isFalling), c -> c.movedRight().canStandIn(grid), Capsule::moveRight);
    }

    public void dipOrAcceptCapsule() {
        performIfPossible(Predicate.not(Capsule::isFalling), c -> c.dipped().canStandIn(grid), Capsule::dip, this::accept);
    }

    private void dipOrAcceptDroppingCapsule() {
        performIfPossible(Capsule::isFalling, c -> c.dipped().canStandIn(grid), Capsule::dip, this::accept);
    }

    public void flipCapsule() {
        performIfPossible(Predicate.not(Capsule::isFalling), c -> c.flipped().canStandIn(grid), Capsule::flip,
          g -> performIfPossible(f -> true, f -> f.flipped().movedBack().canStandIn(grid), f -> {
              f.flip();
              f.moveForward();
          })
        );
    }

    public void dropCapsule() {
        performIfPossible(Predicate.not(Capsule::isFalling), c -> true, Capsule::startFalling);
    }

    public void holdCapsule() {
    }

    // update
    private void spawnCapsule() {
        fallingCapsules.add(Capsule.randomNewInstance(this));
        fallingCapsules.forEach(g -> {
            if (!g.canStandIn(grid)) System.exit(0);
        });
    }

    private void accept(Capsule capsule) {
        capsule.forEachCapsule(grid::put);
        capsule.freeze();
        grid.deleteMatches();
    }

    public void update() {
        fallingCapsules.removeIf(Capsule::isFrozen);
        if (fallingCapsules.stream().noneMatch(Predicate.not(Capsule::isFalling))) spawnCapsule();
        timers.forEach(Timer::resetIfExceeds);
    }
}
