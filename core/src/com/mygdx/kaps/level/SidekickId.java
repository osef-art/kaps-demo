package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.time.PeriodicTask;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum SidekickId {
    SEAN(Color.COLOR_1, AttackType.MELEE, SidekickAttack::hitRandomObjectAndAdjacents, 20, 2),
    ZYRAME(Color.COLOR_2, AttackType.SLICE, SidekickAttack::hit2RandomGerms, 18, 2),
    R3D(Color.COLOR_3, AttackType.SLICE, SidekickAttack::hitRandomColumn, 25, 2, "Red"),
    MIMAPS(Color.COLOR_4, AttackType.FIRE, SidekickAttack::hit3RandomObjects, 15, 2),
    PAINTER(Color.COLOR_5, AttackType.BRUSH, SidekickAttack::paint5RandomObjects, 10, 1, "Paint"),
    XERETH(Color.COLOR_6, AttackType.SLICE, SidekickAttack::hitRandomDiagonals, 25, 1),
    BOMBER(Color.COLOR_7, AttackType.FIREARM, (sdk, lvl) -> SidekickAttack.injectExplosiveCapsule(lvl), 13, true),
    JIM(Color.COLOR_10, AttackType.SLICE, SidekickAttack::hitRandomLine, 18, 1),
    UNI(Color.COLOR_11, AttackType.BRUSH, (sdk, lvl) -> SidekickAttack.injectMonoColorCapsule(lvl), 4, true, "Color"),
    SNIPER(Color.COLOR_12, AttackType.FIREARM, SidekickAttack::hitRandomGerm, 20, 3),
    ;

    final BiFunction<Sidekick, Level, SidekickAttack> attack;
    final AttackType type;
    final boolean passive;
    final String animPath;
    final Color color;
    final int damage;
    final int mana;

    SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> attack,
               int mana, boolean passive, int damage, String... names) {
        var name = names.length > 0 ? names[0] : toString();
        animPath = "android/assets/sprites/sidekicks/" + name + "_";
        this.passive = passive;
        this.attack = attack;
        this.damage = damage;
        this.color = color;
        this.mana = 4;//mana;
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

    public Color color() {
        return color;
    }

    public String getAnimPath() {
        return animPath;
    }

    int gaugeMax() {
        return mana;
    }

    static SidekickId ofName(String name) {
        return Arrays.stream(values())
          .filter(s -> s.toString().equalsIgnoreCase(name))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Can't resolve sidekick of name " + name));
    }
}

class SidekickAttack {
    private final PeriodicTask moveTasks;

    private SidekickAttack(Level level, double speed, Stream<Runnable> stream) {
        LinkedList<Runnable> moves = stream.collect(Collectors.toCollection(LinkedList::new));
        moves.add(level::deleteMatches);
        moveTasks = PeriodicTask.TaskBuilder.everyMilliseconds(speed, () -> moves.removeFirst().run())
          .delayedByMilliseconds(750)
          .endWhen(moves::isEmpty)
          .build();
    }

    private SidekickAttack(Level level, double speed, int iterations, Runnable move) {
        this(level, speed, IntStream.range(0, iterations).mapToObj(n -> move));
    }

    private SidekickAttack(Level level, double speed, Runnable... moves) {
        this(level, speed, Arrays.stream(moves));
    }

    private SidekickAttack(Level level, Runnable move) {
        this(level, 0, move);
    }

    PeriodicTask periodicMoves() {
        return moveTasks;
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
        return new SidekickAttack(lvl, 150, 5, () -> Utils.getOptionalRandomFrom(lvl.getGrid()
          .capsuleStack()
          .filter(o -> o.color() != mate.color())
        ).ifPresent(o -> lvl.repaint(o, mate.color())));
    }

    static SidekickAttack hit3RandomObjects(Sidekick sdk, Level lvl) {
        return new SidekickAttack(lvl, 400, 3,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(o -> lvl.attack(o, sdk))
        );
    }

    static SidekickAttack hitRandomObjectAndAdjacents(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(lvl, 400,
          () -> lvl.attack(picked, sdk),
          () -> Arrays.asList(new Coordinates(0, 1), new Coordinates(0, -1), new Coordinates(1, 0), new Coordinates(-1, 0))
            .forEach(c -> lvl.attack(c.addedTo(picked), sdk.type()))
        );
    }

    static SidekickAttack hit2RandomGerms(Sidekick sdk, Level lvl) {
        return new SidekickAttack(lvl, 600, 2,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    static SidekickAttack hitRandomGerm(Sidekick sdk, Level lvl) {
        return new SidekickAttack(lvl,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    static SidekickAttack hitRandomLine(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(lvl, 100,
          IntStream.range(0, lvl.getGrid().getWidth())
            .mapToObj(n -> new Coordinates(n, picked.y))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack hitRandomColumn(Sidekick sdk, Level lvl) {
        var picked = getRandomTileCoordinates(lvl);
        return new SidekickAttack(lvl, 25,
          IntStream.range(0, lvl.getGrid().getHeight())
            .map(i -> -i).sorted().map(i -> -i)
            .mapToObj(n -> new Coordinates(picked.x, n))
            .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack hitRandomDiagonals(Sidekick sdk, Level lvl) {
        var picked = getRandomObjectCoordinates(lvl);
        return new SidekickAttack(lvl, 25, Stream.of(
            lvl.getGrid().everyTile().stream().filter(c -> c.x - picked.x == c.y - picked.y),
            lvl.getGrid().everyTile().stream().filter(c -> c.x - picked.x == picked.y - c.y)
          )
          .flatMap(Function.identity())
          .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack injectMonoColorCapsule(Level lvl) {
        return new SidekickAttack(lvl, () -> lvl.injectNext(Capsule.randomMonoColorInstance(lvl)));
    }

    static SidekickAttack injectExplosiveCapsule(Level lvl) {
        return injectMonoColorCapsule(lvl);
    }
}
