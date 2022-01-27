package com.mygdx.kaps.level;

import com.badlogic.gdx.ApplicationAdapter;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.Capsule.CapsuleType;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.CooldownGerm;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.PeriodicTask;
import com.mygdx.kaps.time.TaskManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Level extends ApplicationAdapter {
    public static class LevelParameters {
        private final Level model;
        private boolean enablePreview = true;
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
    private final GameView view;
    private final String label;

    private final TaskManager taskManager;
    private final PeriodicTask gridRefresher;
    private final List<LevelObserver> observers;
    private final ParticleManager particleManager;
    private final GameEndManager gameEndManager;

    private final LinkedList<Capsule> upcomingCapsules;
    private final Set<Capsule.CapsuleType> incomingTypes = new HashSet<>();
    private final List<Capsule> controlledCapsules = new ArrayList<>();
    private final List<Sidekick> sidekicks;
    private final Set<Color> colors;
    private final Grid grid;

    Level(String label, Grid gridModel, Set<SidekickId> sidekickSet) {
        this.label = label;
        grid = gridModel;

        sidekicks = sidekickSet.stream()
          .map(Sidekick::ofId)
          .collect(Collectors.toUnmodifiableList());
        colors = Stream.of(sidekicks.stream().map(Sidekick::color), Stream.of(Color.randomBlank()))
          .flatMap(s -> s)
          .collect(Collectors.toUnmodifiableSet());

        do gridModel.stack().forEach(o -> o.repaint(Utils.getRandomFrom(colors)));
        while (!gridModel.getMatches().isEmpty());

        parameters = new LevelParameters(this);
        view = new GameView(this);

        upcomingCapsules = IntStream.range(0, 2)
          .mapToObj(n -> newRandomCapsule())
          .collect(Collectors.toCollection(LinkedList::new));

        taskManager = new TaskManager(
          gridRefresher = PeriodicTask.everySeconds(1, this::dipOrFreezeCapsule),
          PeriodicTask.everyMilliseconds(10, this::dipOrFreezeDroppingCapsules)
        );

        observers = Arrays.asList(
          particleManager = new ParticleManager(this.sidekicks),
          gameEndManager = new GameEndManager(),
          new LevelAttackObserver(this),
          new SoundPlayerObserver()
        );

        spawnCapsule();
    }

    public String getLabel() {
        return label;
    }

    Grid getGrid() {
        return grid;
    }

    double refreshingProgression() {
        return gridRefresher.ratio();
    }

    long getGermsCount() {
        return grid.stack().filter(GridObject::isGerm).count();
    }

    Capsule newRandomCapsule(CapsuleType... types) {
        incomingTypes.addAll(Arrays.asList(types));
        var caps = Capsule.buildRandomInstance(
          new Coordinates(getGrid().getWidth() / 2 - 1, getGrid().getHeight() - 1), colors, incomingTypes
        );
        incomingTypes.clear();
        return caps;
    }

    List<Capsule> controlledCapsules() {
        return controlledCapsules;
    }

    List<Capsule> upcoming() {
        return upcomingCapsules;
    }

    List<Sidekick> getSidekicks() {
        return sidekicks;
    }

    Set<Sidekick> matesOf(Sidekick sidekick) {
        return sidekicks.stream()
          .filter(s -> !s.equals(sidekick))
          .collect(Collectors.toUnmodifiableSet());
    }

    ParticleManager visualParticles() {
        return particleManager;
    }

    GameEndManager gameEndManager() {
        return gameEndManager;
    }

    public LevelParameters parameters() {
        return parameters;
    }

    public boolean isPaused() {
        return parameters.paused;
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
            hitMatches();
            controlledCapsules.forEach(this::updatePreview);
        }
    }

    // grid operations
    private void hit(Coordinates coordinates, int damage) {
        grid.hit(coordinates, damage)
          .filter(GridObject::isDestroyed)
          .ifPresent(obj -> {
              observers.forEach(obs -> obs.onObjectDestroyed(obj));
              obj.triggerEffect(this);
          });
    }

    private void attack(Coordinates coordinates, int damage, AttackType type) {
        if (grid.isInGridBounds(coordinates))
            observers.forEach(obs -> obs.onTileAttack(coordinates, type));
        hit(coordinates, damage);
    }

    public void attack(Coordinates coordinates, AttackType type) {
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

    void hitMatches() {
        var matches = grid.getMatches();
        if (matches.isEmpty()) return;

        matches.values().forEach(s -> {
            int damage = s.size() >= 9 ? 3 : (s.size() >= 5 ? 2 : 1);
            s.forEach(o -> hit(o.coordinates(), damage));
        });
        observers.forEach(obs -> obs.onMatchPerformed(matches));
        grid.initEveryCapsuleDropping();
        fastenGridRefreshing();
    }

    private void accept(Capsule capsule) {
        controlledCapsules.removeIf(c -> c.equals(capsule));
        capsule.applyToBoth(grid::put);
        capsule.startDropping();
        dipOrFreezeDroppingCapsules();
    }

    private void acceptAndSpawnNew(Capsule capsule) {
        accept(capsule);
        decreaseAllCooldowns();

        if (sidekicks.stream().noneMatch(Sidekick::isAttacking))
            spawnCapsuleIfAbsent();
    }

    // update
    private void updatePreview(Capsule capsule) {
        if (parameters.enablePreview) capsule.computePreview(grid);
    }

    void spawnCapsuleIfAbsent() {
        if (controlledCapsules.isEmpty()) {
            spawnCapsule();
            gridRefresher.reset();
        }
    }

    private void spawnCapsule() {
        var upcoming = upcomingCapsules.removeFirst();
        updatePreview(upcoming);
        observers.forEach(o -> o.onCapsuleSpawn(incomingTypes));
        upcomingCapsules.add(0, newRandomCapsule());

        if (upcomingCapsules.size() < 2)
            upcomingCapsules.add(newRandomCapsule());
        controlledCapsules.add(upcoming);
    }

    void triggerSidekicksIfReady() {
        sidekicks.stream()
          .filter(Sidekick::isReady)
          .forEach(sdk -> {
              sdk.trigger(this);
              observers.forEach(o -> o.onSidekickTriggered(sdk));
          });
    }

    void triggerGermsIfReady() {
        grid.cooldownGermStack()
          .filter(CooldownGerm::isReady)
          .forEach(g -> {
              g.trigger(this);
              observers.forEach(o -> o.onGermTriggered(g));
          });
    }

    private void decreaseAllCooldowns() {
        sidekicks.stream()
          .peek(sdk -> sdk.ifPassive(CooldownSidekick::decreaseCooldown))
          .filter(Sidekick::isReady)
          .forEach(sdk -> sdk.ifPassive(s -> {
              s.trigger(this);
              observers.forEach(o -> o.onSidekickTriggered(s));
          }));
        grid.cooldownGermStack().forEach(CooldownGerm::decreaseCooldown);
    }

    private void fastenGridRefreshing() {
        if (gridRefresher.getDuration() > 400_000_000)
            gridRefresher.updateLimit(gridRefresher.getDuration() * .975);
    }

    void prepareNext(Capsule.CapsuleType type) {
        incomingTypes.add(type);
    }

    public void pause() {
        SoundStream.play(SoundStream.SoundStore.PAUSE, 1f);
        taskManager.pauseTasks();
        observers.forEach(LevelObserver::onGamePaused);
    }

    public void resume() {
        taskManager.resumeTasks();
        observers.forEach(LevelObserver::onGameResumed);
    }

    public void render() {
        if (!parameters.paused) {
            observers.forEach(o -> o.onLevelUpdate(this));
            taskManager.update();

            sidekicks.forEach(Sidekick::updateTasks);
            grid.cooldownGermStack().forEach(CooldownGerm::updateTasks);
            view.updateSprites();
        }
        view.render();
    }

    public void dispose() {
        view.dispose();
    }
}
