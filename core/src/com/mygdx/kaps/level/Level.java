package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Level {
    public static class LevelParameters {
        private final Level model;
        private boolean enablePreview;

        private LevelParameters(Level lvl) {
            model = lvl;
        }

        public void togglePreview() {
            enablePreview = !enablePreview;
            model.controlledCapsules.forEach(c -> {
                if (enablePreview) model.updatePreview(c);
                else c.clearPreview();
            });
        }
    }

    private final LevelParameters parameters;
    private final LinkedList<Capsule> upcomingCapsules;
    private final List<Capsule> controlledCapsules = new ArrayList<>();
    private final List<LevelObserver> observers;
    private final List<Timer> timers;
    private final Set<Color> colorSet;
    private final Timer gridRefresher;
    private final Grid grid;

    Level(Grid grid, Set<Color> colors) {
        this.grid = grid;
        colorSet = colors;
        parameters = new LevelParameters(this);
        upcomingCapsules = IntStream.range(0, 2)
          .mapToObj(n -> Capsule.randomNewInstance(this))
          .collect(Collectors.toCollection(LinkedList::new));

        gridRefresher = Timer.ofSeconds(1, this::dipOrAcceptCapsule);
        Timer droppingTimer = Timer.ofMilliseconds(1, this::dipOrAcceptDroppingCapsule);
        timers = Arrays.asList(gridRefresher, droppingTimer);
        observers = new ArrayList<>();
        observers.add(new SoundPlayerObserver());
    }

    Set<Color> getColorSet() {
        return colorSet;
    }

    Grid getGrid() {
        return grid;
    }

    public LevelParameters parameters() {
        return parameters;
    }

    List<Capsule> controlledCapsules() {
        return controlledCapsules;
    }

    List<Capsule> upcoming() {
        return upcomingCapsules;
    }

    Coordinates spawningCoordinates() {
        return new Coordinates(getGrid().getWidth() / 2 - 1, getGrid().getHeight() - 1);
    }

    double refreshingProgression() {
        return gridRefresher.ratio();
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
        List<Capsule> rejected = new ArrayList<>();
        controlledCapsules.stream()
          .filter(selection)
          .forEach(c -> {
              if (condition.test(c)) action.accept(c);
              else rejected.add(c);
          });
        rejected.forEach(alternative);
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
        performIfPossible(selection, condition, action, p -> {
        });
    }

    // capsule moves
    public void moveCapsuleLeft() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.movedLeft().canStandIn(grid), c -> {
            c.moveLeft();
            updatePreview(c);
        }, c -> observers.forEach(LevelObserver::onIllegalMove));
    }

    public void moveCapsuleRight() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.movedRight().canStandIn(grid), c -> {
            c.moveRight();
            updatePreview(c);
        }, c -> observers.forEach(LevelObserver::onIllegalMove));
    }

    public void dipOrAcceptCapsule() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.dipped().canStandIn(grid), Capsule::dip, this::accept);
    }

    private void dipOrAcceptDroppingCapsule() {
        performIfPossible(Capsule::isDropping, c -> c.dipped().canStandIn(grid), Capsule::dip, this::accept);
    }

    public void flipCapsule() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.flipped().canStandIn(grid), c -> {
              observers.forEach(LevelObserver::onCapsuleFlipped);
              c.flip();
              updatePreview(c);
          },
          c -> performIfPossible(f -> true, f -> f.flipped().movedBack().canStandIn(grid), f -> {
                observers.forEach(LevelObserver::onCapsuleFlipped);
                f.flip();
                f.moveForward();
                updatePreview(f);
            },
            f -> observers.forEach(LevelObserver::onIllegalMove)
          )
        );
    }

    public void dropCapsule() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> true, capsule -> {
            observers.forEach(LevelObserver::onCapsuleDrop);
            capsule.startDropping();
        });
    }

    public void holdCapsule() {
    }

    // update
    private boolean checkForGameOver() {
//        return controlledCapsules.stream()
//          .filter(Predicate.not(Capsule::isDropping))
//          .map(c -> !c.canStandIn(grid))
//          .reduce(Boolean::logicalOr)
//          .orElse(false) || grid.germsCount() <= 0;
        return grid.germsCount() <= 0;
    }

    private void updatePreview(Capsule capsule) {
        if (parameters.enablePreview) capsule.updatePreview(grid);
    }

    private void spawnCapsule() {
        var upcoming = upcomingCapsules.removeFirst();
        updatePreview(upcoming);
        upcomingCapsules.add(Capsule.randomNewInstance(this));
        controlledCapsules.add(upcoming);
    }

    private void deleteMatchesRecursively() {
        while (grid.containsMatches()) {
            observers.forEach(LevelObserver::onMatchDeleted);
            grid.deleteMatches();
            grid.dropEveryCapsule();
        }
    }

    private void accept(Capsule capsule) {
        capsule.applyToBoth(grid::put);
        capsule.freeze();
        observers.forEach(LevelObserver::onCapsuleAccepted);
        deleteMatchesRecursively();
        controlledCapsules.removeIf(c-> c.equals(capsule));
        controlledCapsules.forEach(this::updatePreview);
    }

    public void update() {
        grid.updateSprites();
        if (controlledCapsules.isEmpty()) {
            gridRefresher.reset();
            spawnCapsule();
        }
        if (checkForGameOver()) {
            System.out.println("LEVEL CLEARED !");
            System.exit(0);
        }
        timers.forEach(Timer::resetIfExceeds);
    }
}
