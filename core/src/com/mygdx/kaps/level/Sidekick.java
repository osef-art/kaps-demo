package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


interface ISidekick {
    double gaugeRatio();

    void resetGauge();

    boolean isReady();

    void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction);

    default void ifActive(Consumer<ManaSidekick> action) {
        ifActiveElse(action, s -> {});
    }

    default void ifPassive(Consumer<CooldownSidekick> action) {
        ifActiveElse(s -> {}, action);
    }
}

public abstract class Sidekick implements ISidekick {
    public enum AttackType {
        SLICE("slice", SoundStream.SoundStore.SLICE),
        FIRE("fire", SoundStream.SoundStore.FIRE),
        FIREARM("fire", SoundStream.SoundStore.SHOT),
        MELEE("pain", SoundStream.SoundStore.SHOT),
        //        MAGIC("pain", SoundStream.SoundStore.SLICE),
        BRUSH("paint", SoundStream.SoundStore.PAINT),
        ;

        private final SoundStream.SoundStore sound;
        private final String path;

        AttackType(String path, SoundStream.SoundStore sound) {
            this.sound = sound;
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }

        public SoundStream.SoundStore sound() {
            return sound;
        }
    }

    enum SidekickId {
        SEAN(Color.COLOR_1, AttackType.MELEE, SidekickAttack::hitRandomObjectAndAdjacents, 20, 2),
        ZYRAME(Color.COLOR_2, AttackType.SLICE, SidekickAttack::hit2RandomGerms, 18, 2),
        R3D(Color.COLOR_3, AttackType.SLICE, SidekickAttack::hitRandomColumn, 25, 2, "Red"),
        MIMAPS(Color.COLOR_4, AttackType.FIRE, SidekickAttack::hit3RandomObjects, 15, 2),
        PAINTER(Color.COLOR_5, AttackType.BRUSH, SidekickAttack::paint5RandomObjects, 10, 1, "Paint"),
        XERETH(Color.COLOR_6, AttackType.SLICE, SidekickAttack::hitRandomDiagonals, 25, 1),
        BOMBER(Color.COLOR_7, AttackType.FIREARM, SidekickAttack::injectExplosiveCapsule, 13, true),
        JIM(Color.COLOR_10, AttackType.SLICE, SidekickAttack::hitRandomLine, 18, 1),
        UNI(Color.COLOR_11, AttackType.BRUSH, SidekickAttack::injectMonoColorCapsule, 4, true, "Color"),
        SNIPER(Color.COLOR_12, AttackType.FIREARM, SidekickAttack::hitRandomGerm, 20, 3),
        ;

        private final BiFunction<Sidekick, Level, SidekickAttack> attack;
        private final AttackType type;
        private final boolean passive;
        private final String animPath;
        private final Color color;
        private final int damage;
        private final int mana;

        SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> attack, int mana,
                   boolean passive,
                   int damage, String... names) {
            var name = names.length > 0 ? names[0] : toString();
            animPath = "android/assets/sprites/sidekicks/" + name + "_";
            this.passive = passive;
            this.attack = attack;
            this.damage = damage;
            this.color = color;
            this.mana = mana;
            this.type = type;
        }

        SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> atk, int mana, boolean passive, String... names) {
            this(color, type, atk, mana, passive, 0, names);
        }

        SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> atk, int mana, int damage, String... names) {
            this(color, type, atk, mana, false, damage, names);
        }

        @Override
        public String toString() {
            var str = super.toString();
            return str.charAt(0) + str.substring(1).toLowerCase();
        }

        int gaugeMax() {
            return mana;
        }

        private static SidekickId ofName(String name) {
            return Arrays.stream(values())
              .filter(s -> s.toString().equalsIgnoreCase(name))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Can't resolve sidekick of name " + name));
        }
    }

    private final List<SidekickAttack> currentAttacks = new ArrayList<>();
    private final AnimatedSprite flippedAnim;
    private final AnimatedSprite anim;
    private final SidekickId id;

    Sidekick(SidekickId id) {
        flippedAnim = new AnimatedSprite(id.animPath, 4, 0.2f, true, true);
        anim = new AnimatedSprite(id.animPath, 4, 0.2f);
        this.id = id;
    }

    static Sidekick ofId(SidekickId id) {
        return id.passive ? new CooldownSidekick(id) : new ManaSidekick(id);
    }

    public static Sidekick ofName(String name) {
        return ofId(SidekickId.ofName(name));
    }

    static Sidekick random() {
        return ofId(Utils.getRandomFrom(Arrays.stream(SidekickId.values())));
    }

    Sidekick randomMate(Level level) {
        return Utils.getOptionalRandomFrom(level.matesOf(this)).orElse(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sidekick)) return false;
        Sidekick sidekick = (Sidekick) o;
        return id == sidekick.id;
    }

    public Color color() {
        return id.color;
    }

    AttackType type() {
        return id.type;
    }

    int damage() {
        return id.damage;
    }

    Sprite getSprite() {
        return anim.getCurrentSprite();
    }

    Sprite getFlippedSprite() {
        return flippedAnim.getCurrentSprite();
    }

    void updateSprite() {
        anim.updateExistenceTime();
        flippedAnim.updateExistenceTime();
    }

    void updateAttacks(Level level) {
        currentAttacks.forEach(attack -> {
            attack.update();
            if (attack.isOver()) level.deleteMatches();
        });
        currentAttacks.removeIf(SidekickAttack::isOver);
    }

    void trigger(Level level) {
        currentAttacks.add(id.attack.apply(this, level));
        resetGauge();
    }
}

class ManaSidekick extends Sidekick {
    private final Gauge mana;

    ManaSidekick(SidekickId id) {
        super(id);
        this.mana = new Gauge(5); //id.gaugeMax());
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

    void increaseMana() {
        mana.increase();
    }

    public void resetGauge() {
        mana.empty();
    }

    public boolean isReady() {
        return mana.isFull();
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

    void decreaseCooldown() {
        cooldown.decreaseIfPossible();
    }

    public void resetGauge() {
        cooldown.fill();
    }

    public boolean isReady() {
        return cooldown.isEmpty();
    }

    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        passiveAction.accept(this);
    }
}

class SidekickAttack {
    private final LinkedList<Runnable> moves;
    private final Timer moveScheduler;

    private SidekickAttack(double speed, Stream<Runnable> stream) {
        moves = stream.collect(Collectors.toCollection(LinkedList::new));
        moveScheduler = Timer.ofMilliseconds(speed, () -> {
            if (!isOver()) moves.removeFirst().run();
        });
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

    private static Coordinates getRandomTileCoordinates(Level level) {
        return Utils.getRandomFrom(level.getGrid().everyTile());
    }

    private static Coordinates getRandomObjectCoordinates(Level level) {
        return Utils.getOptionalRandomFrom(level.getGrid().stack())
          .filter(Predicate.not(GridObject::isDropping))
          .map(GridObject::coordinates)
          .orElse(getRandomTileCoordinates(level));
    }

    public static SidekickAttack paint5RandomObjects(Sidekick sdk, Level lvl) {
        var mate = sdk.randomMate(lvl);
        return new SidekickAttack(200, 5, () -> Utils.getOptionalRandomFrom(lvl.getGrid()
          .capsuleStack()
          .filter(o -> o.color() != mate.color())
        ).ifPresent(o -> lvl.repaint(o, mate.color())));
    }

    public static SidekickAttack hit3RandomObjects(Sidekick sdk, Level lvl) {
        return new SidekickAttack(250, 3,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(o -> lvl.attack(o, sdk))
        );
    }

    public static SidekickAttack hitRandomObjectAndAdjacents(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(350,
          () -> lvl.attack(picked, sdk),
          () -> Arrays.asList(new Coordinates(0, 1), new Coordinates(0, -1), new Coordinates(1, 0), new Coordinates(-1, 0))
            .forEach(c -> lvl.attack(c.addedTo(picked), sdk.type()))
        );
    }

    public static SidekickAttack hit2RandomGerms(Sidekick sdk, Level lvl) {
        return new SidekickAttack(400, 2,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    public static SidekickAttack hitRandomGerm(Sidekick sdk, Level lvl) {
        return new SidekickAttack(
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    public static SidekickAttack hitRandomLine(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(100,
          IntStream.range(0, lvl.getGrid().getWidth())
            .mapToObj(n -> new Coordinates(n, picked.y))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    public static SidekickAttack hitRandomColumn(Sidekick sdk, Level lvl) {
        var picked = getRandomTileCoordinates(lvl);
        return new SidekickAttack(75,
          IntStream.range(0, lvl.getGrid().getHeight())
            .mapToObj(n -> new Coordinates(picked.x, n))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    public static SidekickAttack hitRandomDiagonals(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(50,
          lvl.getGrid().everyTile().stream()
            .filter(c -> Math.abs(c.x - picked.x) == Math.abs(c.y - picked.y))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    public static SidekickAttack injectMonoColorCapsule(Sidekick sdk, Level lvl) {
        return new SidekickAttack(() -> lvl.injectNext(Capsule.randomMonoColorInstance(lvl)));
    }

    public static SidekickAttack injectExplosiveCapsule(Sidekick sdk, Level lvl) {
        return new SidekickAttack(() -> {});
    }

    void update() {
        moveScheduler.resetIfExceeds();
    }
}
