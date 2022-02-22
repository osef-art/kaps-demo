package com.mygdx.kaps.level;

import com.badlogic.gdx.ApplicationAdapter;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.Capsule.CapsuleType;
import com.mygdx.kaps.level.gridobject.*;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.PeriodicTask;
import com.mygdx.kaps.time.TaskManager;
import com.mygdx.kaps.time.Timer;

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
    private final ScreenShaker screenShaker;
    private final ScoreManager scoreManager;

    private boolean canHold;
    private Capsule heldCapsule = null;
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
          scoreManager = new ScoreManager(),
          new LevelAttackObserver(),
          screenShaker = new ScreenShaker(),
          new SoundPlayer()
        );

        spawnCapsule();
    }

    String getLabel() {
        return label;
    }

    Grid getGrid() {
        return grid;
    }

    boolean capsuleCanBeHeld() {
        return canHold;
    }

    double refreshingProgression() {
        return gridRefresher.ratio();
    }

    long getGermsCount() {
        return grid.germStack().count();
    }

    Capsule newRandomCapsule(CapsuleType... types) {
        incomingTypes.addAll(Arrays.asList(types));
        var caps = Capsule.buildRandomInstance(
          spawnCoordinates(), colors, incomingTypes
        );
        incomingTypes.clear();
        return caps;
    }

    private Coordinates spawnCoordinates() {
        return new Coordinates(getGrid().getWidth() / 2 - 1, getGrid().getHeight() - 1);
    }

    List<Capsule> controlledCapsules() {
        return controlledCapsules;
    }

    List<Capsule> upcoming() {
        return upcomingCapsules;
    }

    Optional<Capsule> getHeldCapsule() {
        return Optional.ofNullable(heldCapsule);
    }

    List<Sidekick> getSidekicks() {
        return sidekicks;
    }

    Set<Sidekick> matesOf(Sidekick sidekick) {
        return sidekicks.stream()
          .filter(s -> !s.equals(sidekick))
          .collect(Collectors.toUnmodifiableSet());
    }

    List<Timer> quakes() {
        return screenShaker.currentQuakes();
    }

    ParticleManager visualParticles() {
        return particleManager;
    }

    GameEndManager gameEndManager() {
        return gameEndManager;
    }

    ScoreManager getScoreData() {
        return scoreManager;
    }

    public LevelParameters parameters() {
        return parameters;
    }

    public boolean isOver() {
        return gameEndManager.gameIsOver(this);
    }

    boolean isPaused() {
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
            observers.forEach(o -> o.onCapsuleFreeze(this));
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

    public void holdCapsule() {
        performIfPossible(c -> canHold, c -> {
            controlledCapsules.remove(c);
            getHeldCapsule().ifPresent(held -> controlledCapsules.add(held.copy(c)));
            heldCapsule = c.copy(spawnCoordinates(), Orientation.LEFT);
            spawnCapsuleIfAbsent();
            gridRefresher.reset();
            observers.forEach(LevelObserver::onCapsuleHold);
            canHold = false;
        });
    }

    private void dipOrFreezeDroppingCapsules() {
        if (grid.dipOrFreezeDroppingCapsules()) {
            observers.forEach(o -> o.onCapsuleFreeze(this));
            hitMatches();
            controlledCapsules.forEach(this::updatePreview);
        }
    }

    // grid operations
    private void hit(Coordinates coordinates, int damage) {
        grid.hit(coordinates, damage)
          .ifPresent(obj -> {
              observers.forEach(obs -> obs.onObjectHit(obj, damage));
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

        matches.forEach(
          m -> m.stream().forEach(o -> hit(o.coordinates(), m.dependingOnSize(1, 2, 3)))
        );
        observers.forEach(obs -> obs.onMatchPerformed(matches, scoreManager.currentCombo()));
        if (matches.isEmpty()) return;

        grid.initEveryCapsuleDropping();
        fastenGridRefreshing();
    }

    private void accept(Capsule capsule) {
        controlledCapsules.remove(capsule);
        capsule.applyToBoth(grid::put);
        capsule.startDropping();
        dipOrFreezeDroppingCapsules();
    }

    private void acceptAndSpawnNew(Capsule capsule) {
        accept(capsule);
        decreaseAllCooldowns();

        if (sidekicks.stream().noneMatch(Sidekick::isReady))
            spawnCapsuleIfAbsent();
    }

    // update
    private void updatePreview(Capsule capsule) {
        if (parameters.enablePreview) capsule.computePreview(grid);
    }

    private void spawnCapsule() {
        var upcoming = upcomingCapsules.removeFirst();
        updatePreview(upcoming);
        observers.forEach(o -> o.onCapsuleSpawn(incomingTypes));
        upcomingCapsules.add(0, newRandomCapsule());
        canHold = true;

        if (upcomingCapsules.size() < 2)
            upcomingCapsules.add(newRandomCapsule());
        controlledCapsules.add(upcoming);
    }

    void spawnCapsuleIfAbsent() {
        if (controlledCapsules.isEmpty()) {
            spawnCapsule();
            gridRefresher.reset();
        }
    }

    void triggerSidekicksIfReady() {
        sidekicks.stream()
          .filter(Sidekick::isReady)
          .forEach(sdk -> {
              if (sdk.isAttacking()) return;
              sdk.trigger(this);
              observers.forEach(o -> o.onSidekickTriggered(sdk));
          });
    }

    void triggerGermsIfReady() {
        grid.germStack()
          .forEach(g -> g.ifHasCooldown(cg -> {
              if (!cg.isReady()) return;
              cg.trigger(this);
              observers.forEach(o -> o.onGermTriggered(cg));
          }));
    }

    private void decreaseAllCooldowns() {
        sidekicks.stream()
          .peek(sdk -> sdk.ifPassive(CooldownSidekick::decreaseCooldown))
          .filter(Sidekick::isReady)
          .forEach(sdk -> sdk.ifPassive(s -> {
              s.trigger(this);
              observers.forEach(o -> o.onSidekickTriggered(s));
          }));
        grid.germStack()
          .forEach(g -> g.ifHasCooldown(CooldownGerm::decreaseCooldown));
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
            taskManager.update();
            observers.forEach(LevelObserver::onLevelUpdate);

            sidekicks.forEach(Sidekick::updateTasks);
            grid.germStack().forEach(g -> g.ifHasCooldown(CooldownGerm::updateTasks));
            view.updateSprites();
        }
        view.render();
    }

    public void dispose() {
        view.dispose();
    }
}
