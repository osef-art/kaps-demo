package com.mygdx.kaps.level;

import com.badlogic.gdx.ApplicationAdapter;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.RegularTask;
import com.mygdx.kaps.time.RegularTaskManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Level extends ApplicationAdapter {
    public static class LevelParameters {
        private final Level model;
        private boolean enablePreview;
        private boolean paused;

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

        public void togglePause() {
            paused = !paused;
            if (paused) model.pause();
            else model.resume();
        }
    }

    private final LevelParameters parameters;
    private final ParticleManager particleManager;
    private final List<LevelObserver> observers;
    private final LinkedList<Capsule> upcomingCapsules;
    private final List<Capsule> controlledCapsules = new ArrayList<>();
    private final List<Sidekick> sidekicks;
    private final Set<Color> colors;
    private final RegularTaskManager taskManager;
    private final RegularTask gridRefresher;
    private final GameView view;
    private final Grid grid;

    Level(Grid grid, Set<Sidekick.SidekickId> sidekicks, Color blankColor) {
        this.grid = grid;
        parameters = new LevelParameters(this);

        this.sidekicks = sidekicks.stream()
          .map(Sidekick::ofId)
          .collect(Collectors.toUnmodifiableList());
        colors = Color.getSetFrom(sidekicks, blankColor);
        sidekicks.forEach(s -> {
            if (!colors.contains(s.color())) throw new IllegalArgumentException("Insufficient color set.");
        });

        upcomingCapsules = IntStream.range(0, 2)
          .mapToObj(n -> Capsule.randomNewInstance(this))
          .collect(Collectors.toCollection(LinkedList::new));

        gridRefresher = RegularTask.everySeconds(1, this::dipOrFreezeCapsule);
        taskManager = new RegularTaskManager(
          gridRefresher, RegularTask.everyMilliseconds(10, this::dipOrFreezeDroppingCapsules)
        );

        particleManager = new ParticleManager(this.sidekicks);
        observers = Arrays.asList(
          new SoundPlayerObserver(),
          new SidekicksObserver(this.sidekicks),
          new GameEndManager(this),
          particleManager
        );
        view = new GameView(this);

        spawnCapsule();
    }

    List<Sidekick> getSidekicks() {
        return sidekicks;
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

    ParticleManager visualParticles() {
        return particleManager;
    }

    double refreshingProgression() {
        return gridRefresher.ratio();
    }

    /**
     * Executes on each level's capsules matching {@param selection} the code conveyed by {@param action}
     * if {@param condition} is true, else executes {@param alternative} instead
     *
     * @param condition   the predicate to match to execute {@param action}
     * @param action      the action to execute on each falling Capsule
     * @param alternative the action to execute on each falling Capsule if {@param condition} is not matched
     */
    private void performIfPossible(Predicate<Capsule> condition, Consumer<Capsule> action, Consumer<Capsule> alternative) {
        if (parameters.paused) return;
        controlledCapsules.stream()
          .filter(Predicate.not(Capsule::isDropping))
          .collect(Collectors.toUnmodifiableMap(condition::test, Arrays::asList,
            (l1, l2) -> Stream.of(l1, l2).flatMap(Collection::stream).collect(Collectors.toList()))
          )
          .forEach((accepted, capsules) -> capsules.forEach(accepted ? action : alternative));
    }

    private void performIfPossible(Predicate<Capsule> condition, Consumer<Capsule> action) {
        performIfPossible(condition, action, c -> observers.forEach(LevelObserver::onIllegalMove));
    }

    // capsule moves
    public void moveCapsuleLeft() {
        performIfPossible(c -> c.movedLeft().canStandIn(grid), c -> {
            c.moveLeft();
            updatePreview(c);
        });
    }

    public void moveCapsuleRight() {
        performIfPossible(c -> c.movedRight().canStandIn(grid), c -> {
            c.moveRight();
            updatePreview(c);
        });
    }

    public void dipOrFreezeCapsule() {
        performIfPossible(c -> c.dipped().canStandIn(grid), c -> {
              c.dip();
              gridRefresher.reset();
          }, c -> {
              acceptAndSpawnNew(c);
              observers.forEach(LevelObserver::onCapsuleFreeze);
          });
    }

    public void flipCapsule() {
        performIfPossible(c -> c.flipped().canStandIn(grid), c -> {
              observers.forEach(LevelObserver::onCapsuleFlipped);
              c.flip();
              updatePreview(c);
          },
          c -> performIfPossible(f -> f.flipped().movedBack().canStandIn(grid), f -> {
              observers.forEach(LevelObserver::onCapsuleFlipped);
              f.flip();
              f.moveForward();
              updatePreview(f);
          })
        );
    }

    public void dropCapsule() {
        performIfPossible(c -> true, c -> {
            acceptAndSpawnNew(c);
            observers.forEach(LevelObserver::onCapsuleDrop);
        });
    }

    public void holdCapsule() {}

    private void dipOrFreezeDroppingCapsules() {
        if (grid.dipOrFreezeDroppingCapsules()) {
            observers.forEach(LevelObserver::onCapsuleFreeze);
            deleteMatches();
        }
    }

    // grid operations
    private void attack(Coordinates coordinates, int damage, AttackType type) {
        if (grid.isInGridBounds(coordinates))
            observers.forEach(obs -> obs.onTileAttack(coordinates, type));
        grid.hit(coordinates, damage).ifPresent(
          obj -> observers.forEach(obs -> obs.onObjectHit(obj))
        );
    }

    void attack(Coordinates coordinates, AttackType type) {
        attack(coordinates, 1, type);
    }

    void attack(Coordinates coordinates, Sidekick sdk) {
        attack(coordinates, sdk.damage(), sdk.type());
    }

    void attack(GridObject obj, Sidekick sdk) {
        attack(obj.coordinates(), sdk);
    }

    void repaint(GridObject obj, Color color) {
        grid.repaint(obj, color);
        observers.forEach(o -> o.onObjectPaint(obj, color));
    }

    void deleteMatches() {
        var destroyed = grid.hitMatches();
        if (!destroyed.isEmpty()) {
            observers.forEach(obs -> obs.onMatchPerformed(destroyed));
            fastenGridRefreshing();
        }
        grid.initEveryCapsuleDropping();
    }

    private void accept(Capsule capsule) {
        controlledCapsules.removeIf(c -> c.equals(capsule));
        capsule.applyToBoth(grid::put);
        capsule.startDropping();
        dipOrFreezeDroppingCapsules();
    }

    private void acceptAndSpawnNew(Capsule capsule) {
        accept(capsule);

        if (controlledCapsules.isEmpty()) {
            observers.forEach(LevelObserver::onCapsuleSpawn);
            triggerSidekicksIfReady();
            spawnCapsule();
            gridRefresher.reset();
        }
    }

    // update
    private void updatePreview(Capsule capsule) {
        if (parameters.enablePreview) capsule.updatePreview(grid);
    }

    private void spawnCapsule() {
        var upcoming = upcomingCapsules.removeFirst();
        updatePreview(upcoming);
        if (upcomingCapsules.size() < 2)
            upcomingCapsules.add(Capsule.randomNewInstance(this));
        controlledCapsules.add(upcoming);
    }

    private void triggerSidekicksIfReady() {
        sidekicks.stream()
          .filter(Sidekick::isReady)
          .forEach(sdk -> {
              sdk.trigger(this);
              observers.forEach(o -> o.onSidekickTriggered(sdk));
          });
    }

    private void fastenGridRefreshing() {
        gridRefresher.updateLimit(gridRefresher.getLimit() * 0.975);
    }

    void injectNext(Capsule capsule) {
        upcomingCapsules.add(0, capsule);
    }

    @Override
    public void pause() {
        SoundStream.play(SoundStream.SoundStore.PAUSE, 1f);
        taskManager.pauseTasks();

    }

    @Override
    public void resume() {
        taskManager.resumeTasks();
    }

    public void render() {
        if (!parameters.paused) {
            observers.forEach(LevelObserver::onLevelUpdate);
            taskManager.update();

            sidekicks.forEach(s -> s.updateAttacks(this));
            view.updateSprites();
        }
        view.render();
    }

    public void dispose() {
        view.dispose();
    }
}
