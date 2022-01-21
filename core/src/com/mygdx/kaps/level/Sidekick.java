package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.time.PeriodicTask;
import com.mygdx.kaps.time.TaskManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


interface ISidekick {
    double gaugeRatio();

    boolean isReady();

    boolean gaugeIsReset();

    void emptyGauge();

    void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction);

    default void ifActive(Consumer<ManaSidekick> action) {
        ifActiveElse(action, s -> {});
    }

    default void ifPassive(Consumer<CooldownSidekick> action) {
        ifActiveElse(s -> {}, action);
    }
}

public abstract class Sidekick implements ISidekick {
    private static final Map<SidekickId, Sidekick> sidekicks =
      Arrays.stream(SidekickId.values()).collect(Collectors.toUnmodifiableMap(
        Function.identity(), id -> id.passive ? new CooldownSidekick(id) : new ManaSidekick(id)
      ));
    private final TaskManager tasks = new TaskManager();
    private final SidekickId id;

    Sidekick(SidekickId id) {
        this.id = id;
    }

    static Sidekick ofId(SidekickId id) {
        return sidekicks.get(id);
    }

    Sidekick randomMate(Level level) {
        return Utils.getOptionalRandomFrom(level.matesOf(this)).orElse(this);
    }

    SidekickId id() {
        return id;
    }

    Color color() {
        return id.color;
    }

    AttackType type() {
        return id.type;
    }

    int damage() {
        return id.damage;
    }

    boolean isAttacking() {
        return !tasks.isEmpty();
    }

    void updateTasks() {
        tasks.update();
    }

    void trigger(Level level) {
        tasks.add(id.attack.apply(this, level).moveProgression(), level::deleteMatches);
        tasks.add(PeriodicTask.everyMilliseconds(10, this::emptyGauge, this::gaugeIsReset));
    }
}

class ManaSidekick extends Sidekick {
    private final Gauge mana;

    ManaSidekick(SidekickId id) {
        super(id);
        this.mana = new Gauge(id.gaugeMax());
    }

    public double gaugeRatio() {
        return mana.ratio();
    }

    int currentMana() {
        return mana.getValue();
    }

    int maxMana() {
        return mana.getMax();
    }

    public boolean isReady() {
        return mana.isFull();
    }

    public boolean gaugeIsReset() {
        return mana.isEmpty();
    }

    void increaseMana() {
        mana.increase();
    }

    public void emptyGauge() {
        mana.decreaseIfPossible();
    }

    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        activeAction.accept(this);
    }
}

class CooldownSidekick extends Sidekick {
    private final Gauge cooldown;

    CooldownSidekick(SidekickId id) {
        super(id);
        this.cooldown = Gauge.full(id.gaugeMax());
    }

    public double gaugeRatio() {
        return cooldown.ratio();
    }

    int turnsLeft() {
        return cooldown.getValue();
    }

    public boolean isReady() {
        return cooldown.isEmpty();
    }

    public boolean gaugeIsReset() {
        return cooldown.isFull();
    }

    void decreaseCooldown() {
        cooldown.decreaseIfPossible();
    }

    public void emptyGauge() {
        cooldown.increaseIfPossible();
    }

    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        passiveAction.accept(this);
    }
}

class SidekickAttack {
    private final LinkedList<Runnable> moves;
    private final PeriodicTask moveScheduler;

    private SidekickAttack(double speed, Stream<Runnable> stream) {
        moves = stream.collect(Collectors.toCollection(LinkedList::new));
        moveScheduler = PeriodicTask.everyMilliseconds(speed, () -> moves.removeFirst().run(), this::isOver);
    }

    private SidekickAttack(double speed, int iterations, Runnable move) {
        this(speed, IntStream.range(0, iterations).mapToObj(n -> move));
    }

    private SidekickAttack(double speed, Runnable... moves) {
        this(speed, Arrays.stream(moves));
    }

    private SidekickAttack(Runnable move) {
        this(0, move);
    }

    boolean isOver() {
        return moves.isEmpty();
    }

    PeriodicTask moveProgression() {
        return moveScheduler;
    }

    private static Coordinates getRandomTileCoordinates(Level level) {
        return Utils.getRandomFrom(level.getGrid().everyTile());
    }

    private static Coordinates getRandomObjectCoordinates(Level level) {
        return Utils.getOptionalRandomFrom(level.getGrid().stack())
          .filter(Predicate.not(GridObject::isDropping))
          .map(GridObject::coordinates)
          .orElse(getRandomTileCoordinates(level));
    }

    static SidekickAttack paint5RandomObjects(Sidekick sdk, Level lvl) {
        var mate = sdk.randomMate(lvl);
        return new SidekickAttack(150, 5, () -> Utils.getOptionalRandomFrom(lvl.getGrid()
          .capsuleStack()
          .filter(o -> o.color() != mate.color())
        ).ifPresent(o -> lvl.repaint(o, mate.color())));
    }

    static SidekickAttack hit3RandomObjects(Sidekick sdk, Level lvl) {
        return new SidekickAttack(250, 3,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(o -> lvl.attack(o, sdk))
        );
    }

    static SidekickAttack hitRandomObjectAndAdjacents(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(350,
          () -> lvl.attack(picked, sdk),
          () -> Arrays.asList(new Coordinates(0, 1), new Coordinates(0, -1), new Coordinates(1, 0), new Coordinates(-1, 0))
            .forEach(c -> lvl.attack(c.addedTo(picked), sdk.type()))
        );
    }

    static SidekickAttack hit2RandomGerms(Sidekick sdk, Level lvl) {
        return new SidekickAttack(400, 2,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    static SidekickAttack hitRandomGerm(Sidekick sdk, Level lvl) {
        return new SidekickAttack(
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    static SidekickAttack hitRandomLine(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(100,
          IntStream.range(0, lvl.getGrid().getWidth())
            .mapToObj(n -> new Coordinates(n, picked.y))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack hitRandomColumn(Sidekick sdk, Level lvl) {
        var picked = getRandomTileCoordinates(lvl);
        return new SidekickAttack(25,
          IntStream.range(0, lvl.getGrid().getHeight())
            .map(i -> -i).sorted().map(i -> -i)
            .mapToObj(n -> new Coordinates(picked.x, n))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack hitRandomDiagonals(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(25, Stream.of(
            lvl.getGrid().everyTile().stream().filter(c -> c.x - picked.x == c.y - picked.y),
            lvl.getGrid().everyTile().stream().filter(c -> c.x - picked.x == picked.y - c.y)
          )
          .flatMap(Function.identity())
          .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack injectMonoColorCapsule(Level lvl) {
        return new SidekickAttack(() -> lvl.injectNext(Capsule.randomMonoColorInstance(lvl)));
    }

    static SidekickAttack injectExplosiveCapsule(Level lvl) {
        return injectMonoColorCapsule(lvl);
    }
}
