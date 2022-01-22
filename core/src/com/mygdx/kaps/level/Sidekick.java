package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.time.PeriodicTask;
import com.mygdx.kaps.time.TaskManager;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


interface ISidekick {
    double gaugeRatio();

    boolean isReady();

    boolean gaugeIsReset();

    void emptyGauge();

    void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction);

    <T> T ifActiveElse(T activeValue, T passiveValue);

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
        tasks.add(id.attack.apply(this, level).periodicMoves());
        tasks.add(
          PeriodicTask.TaskBuilder.everyMilliseconds(10, this::emptyGauge).endWhen(this::gaugeIsReset)
        );
    }
}

class ManaSidekick extends Sidekick {
    private final Gauge mana;

    ManaSidekick(SidekickId id) {
        super(id);
        this.mana = new Gauge(id.gaugeMax());
    }

    @Override
    public double gaugeRatio() {
        return mana.ratio();
    }

    int currentMana() {
        return mana.getValue();
    }

    int maxMana() {
        return mana.getMax();
    }

    @Override
    public boolean isReady() {
        return mana.isFull();
    }

    @Override
    public boolean gaugeIsReset() {
        return mana.isEmpty();
    }

    void increaseMana() {
        mana.increase();
    }

    @Override
    public void emptyGauge() {
        mana.decreaseIfPossible();
    }

    @Override
    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        activeAction.accept(this);
    }

    @Override
    public <T> T ifActiveElse(T activeValue, T passiveValue) {
        return activeValue;
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

    @Override
    public boolean isReady() {
        return cooldown.isEmpty();
    }

    @Override
    public boolean gaugeIsReset() {
        return cooldown.isFull();
    }

    void decreaseCooldown() {
        cooldown.decreaseIfPossible();
    }

    @Override
    public void emptyGauge() {
        cooldown.increaseIfPossible();
    }

    @Override
    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        passiveAction.accept(this);
    }

    @Override
    public <T> T ifActiveElse(T activeValue, T passiveValue) {
        return passiveValue;
    }
}
