package com.mygdx.kaps.level;

import com.mygdx.kaps.level.gridobject.Capsule;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
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
    private final ParticleManager particleManager;
    private final List<LevelObserver> observers;
    private final LinkedList<Capsule> upcomingCapsules;
    private final List<Capsule> controlledCapsules = new ArrayList<>();
    private final List<Sidekick> sidekicks;
    private final Set<Color> colors;
    private final List<Timer> timers;
    private final Timer gridRefresher;
    private final Grid grid;

    Level(Grid grid, Set<Sidekick> sidekicks, Color blankColor) {
        this.grid = grid;
        parameters = new LevelParameters(this);
        this.sidekicks = new ArrayList<>(sidekicks);
        colors = Color.getSetFrom(sidekicks, blankColor);
        sidekicks.forEach(s -> {
            if (!colors.contains(s.color())) throw new IllegalArgumentException("Insufficient color set.");
        });

        upcomingCapsules = IntStream.range(0, 2)
          .mapToObj(n -> Capsule.randomNewInstance(this))
          .collect(Collectors.toCollection(LinkedList::new));

        gridRefresher = Timer.ofSeconds(1, this::dipOrAcceptCapsule);
        Timer droppingTimer = Timer.ofMilliseconds(10, this::dipOrFreezeDroppingCapsules);
        timers = Arrays.asList(gridRefresher, droppingTimer);

        particleManager = new ParticleManager();
        observers = Arrays.asList(
          new SoundPlayerObserver(),
          new SidekicksObserver(this, this.sidekicks),
          particleManager
        );

        spawnCapsule();
    }

    Sidekick getSidekick(int index) {
        return sidekicks.get(index);
    }

    Set<Sidekick> matesOf(Sidekick sidekick) {
        return sidekicks.stream()
          .filter(s -> !s.equals(sidekick))
          .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Color> getColorSet() {
        return colors;
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

    public Coordinates spawningCoordinates() {
        return new Coordinates(getGrid().getWidth() / 2 - 1, getGrid().getHeight() - 1);
    }

    List<ParticleManager.PoppingObject> visualParticles() {
        return particleManager.getPoppingObjects();
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
        performIfPossible(Predicate.not(Capsule::isDropping), c -> c.dipped().canStandIn(grid), capsule -> {
            capsule.dip();
            gridRefresher.reset();
        }, c -> {
            acceptAndSpawnNew(c);
            observers.forEach(LevelObserver::onCapsuleFreeze);
        });
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
        dipOrAcceptCapsule();
        observers.forEach(LevelObserver::onCapsuleDrop);
        acceptAndSpawnNew(controlledCapsules.get(0));
    }

    public void holdCapsule() {
    }

    private void dipOrFreezeDroppingCapsules() {
        if (grid.dipOrFreezeDroppingCapsules()) {
            observers.forEach(LevelObserver::onCapsuleFreeze);
            deleteMatchesIfAny();
        }
    }

    // update
    private boolean gameIsOver() {
        return grid.germsCount() <= 0 || controlledCapsules.stream()
          .map(c -> !c.canStandIn(grid))
          .reduce(Boolean::logicalOr)
          .orElse(false);
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

    private void deleteMatchesIfAny() {
        if (grid.containsMatches()) {
            var destroyed = grid.hitMatches();
            observers.forEach(obs -> obs.onMatchPerformed(destroyed));
            grid.initEveryCapsuleDropping();
        }
    }

    private void accept(Capsule capsule) {
        controlledCapsules.removeIf(c -> c.equals(capsule));
        capsule.startDropping();
        capsule.applyToBoth(grid::put);
    }

    private void acceptAndSpawnNew(Capsule capsule) {
        accept(capsule);

        if (controlledCapsules.isEmpty()) {
            gridRefresher.reset();
            spawnCapsule();
            observers.forEach(LevelObserver::onCapsuleSpawn);
        }
    }

    public void update() {
        sidekicks.forEach(Sidekick::updateSprite);
        observers.forEach(LevelObserver::onLevelUpdate);
        if (gameIsOver()) {
            System.out.println("LEVEL CLEARED !");
            System.exit(0);
        }
        timers.forEach(Timer::resetIfExceeds);
    }
}
