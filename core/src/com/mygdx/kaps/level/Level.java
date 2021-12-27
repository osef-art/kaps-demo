package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
            model.fallingCapsules.forEach(c -> {
                if (enablePreview) model.updatePreview(c);
                else c.clearPreview();
            });
        }
    }

    private final LevelParameters parameters;
    private final LinkedList<Capsule> upcomingCapsules = new LinkedList<>();
    private final List<Capsule> fallingCapsules = new ArrayList<>();
    private final List<Timer> timers = new ArrayList<>();
    private final Set<Color> colors;
    private final Grid grid;

    // TODO: replace by sidekick set
    public Level(Set<Color> colors) {
        colors.add(Color.randomBlank());
        this.colors = colors;

        parameters = new LevelParameters(this);
        grid = new Grid(6, 15);
        IntStream.range(0, 2).forEach(n -> upcomingCapsules.add(Capsule.randomNewInstance(this)));

        var dippingTimer = Timer.ofSeconds(1, this::dipOrAcceptCapsule);
        var droppingTimer = Timer.ofMilliseconds(1, this::dipOrAcceptDroppingCapsule);
        timers.addAll(Arrays.asList(dippingTimer, droppingTimer));
    }

    Set<Color> getColors() {
        return colors;
    }

    Grid getGrid() {
        return grid;
    }

    public LevelParameters parameters() {
        return parameters;
    }

    List<Capsule> fallingCapsules() {
        return fallingCapsules;
    }

    public List<Capsule> upcoming() {
        return upcomingCapsules;
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
        performIfPossible(selection, condition, action, p -> {
        });
    }

    // capsule moves
    public void moveCapsuleLeft() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.movedLeft().canStandIn(grid), c -> {
            c.moveLeft();
            updatePreview(c);
        });
    }

    public void moveCapsuleRight() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.movedRight().canStandIn(grid), c -> {
            c.moveRight();
            updatePreview(c);
        });
    }

    public void dipOrAcceptCapsule() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.dipped().canStandIn(grid), Capsule::dip, this::accept);
    }

    private void dipOrAcceptDroppingCapsule() {
        performIfPossible(Capsule::isDropping, c -> c.dipped().canStandIn(grid), Capsule::dip, this::accept);
    }

    public void flipCapsule() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.flipped().canStandIn(grid), c -> {
              c.flip();
              updatePreview(c);
          },
          c -> performIfPossible(f -> true, f -> f.flipped().movedBack().canStandIn(grid), f -> {
              f.flip();
              f.moveForward();
              updatePreview(f);
          })
        );
    }

    public void dropCapsule() {
        performIfPossible(Predicate.not(Capsule::isDropping), c -> true, Capsule::startDropping);
    }

    public void holdCapsule() {
    }

    // update
    private void updatePreview(Capsule capsule) {
        if (parameters.enablePreview) capsule.updatePreview(grid);
    }

    private void spawnCapsule() {
        var upcoming = upcomingCapsules.removeFirst();
        upcomingCapsules.add(Capsule.randomNewInstance(this));

        fallingCapsules.add(upcoming);
        fallingCapsules.stream()
          .filter(Predicate.not(Capsule::isDropping))
          .forEach(c -> {
              if (!c.canStandIn(grid)) System.exit(0);
              updatePreview(c);
          });
    }

    private void accept(Capsule capsule) {
        capsule.applyToBoth(grid::put);
        capsule.freeze();
        grid.deleteMatchesRecursively();
        fallingCapsules.forEach(this::updatePreview);
    }

    public void update() {
        fallingCapsules.removeIf(Capsule::isFrozen);
        if (fallingCapsules.stream().noneMatch(Predicate.not(Capsule::isDropping))) spawnCapsule();
        timers.forEach(Timer::resetIfExceeds);
    }
}
