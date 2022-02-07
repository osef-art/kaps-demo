package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.CapsulePart;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.Germ;
import com.mygdx.kaps.level.gridobject.GridObject;
import com.mygdx.kaps.time.PeriodicTask;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

abstract class LevelAttack {
    private final PeriodicTask moveTasks;

    LevelAttack(Level level, double speed, Stream<Runnable> stream) {
        LinkedList<Runnable> moves = stream.collect(Collectors.toCollection(LinkedList::new));
        moves.add(() -> {
            level.hitMatches();
            level.getGrid().initEveryCapsuleDropping();
        });
        moves.add(level::spawnCapsuleIfAbsent);
        moveTasks = PeriodicTask.TaskBuilder.everyMilliseconds(speed, () -> moves.removeFirst().run())
          .endWhen(moves::isEmpty)
          .build();
    }

    public PeriodicTask periodicMoves() {
        return moveTasks;
    }

    static Coordinates getRandomTileCoordinates(Level level) {
        return Utils.getRandomFrom(level.getGrid().everyTile());
    }

    static Optional<GridObject> getRandomObject(Level level) {
        return Utils.getOptionalRandomFrom(level.getGrid().stack()
          .filter(Predicate.not(GridObject::isDropping))
        );
    }

    static Optional<CapsulePart> getRandomCapsule(Level level) {
        return Utils.getOptionalRandomFrom(level.getGrid().capsuleStack()
          .filter(Predicate.not(GridObject::isDropping))
        );
    }
}

class SidekickAttack extends LevelAttack {
    private SidekickAttack(Level level, double speed, Stream<Runnable> stream) {
        super(level, speed, stream);
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
        var picked = getRandomObject(lvl)
          .map(GridObject::coordinates)
          .orElse(getRandomTileCoordinates(lvl));
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
        return new SidekickAttack(lvl, 750,
          () -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack()).ifPresent(g -> lvl.attack(g, sdk))
        );
    }

    static SidekickAttack hitRandomLine(Sidekick sdk, Level lvl) {
        var picked = getRandomObject(lvl)
          .map(GridObject::coordinates)
          .orElse(getRandomTileCoordinates(lvl));
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
        var picked = getRandomObject(lvl)
          .map(GridObject::coordinates)
          .orElse(getRandomTileCoordinates(lvl));
        return new SidekickAttack(lvl, 25, Stream.of(
            lvl.getGrid().everyTile().filter(c -> c.x - picked.x == picked.y - c.y),
            IntStream.range(0, 10).mapToObj(n -> new Coordinates(-1, -1)),
            lvl.getGrid().everyTile().filter(c -> c.x - picked.x == c.y - picked.y).sorted(Comparator.comparingInt(c -> -c.y))
          )
          .flatMap(Function.identity())
          .map(c -> () -> lvl.attack(c, sdk))
        );
    }

    static SidekickAttack injectUniformCapsule(Level lvl) {
        return new SidekickAttack(lvl, () -> lvl.prepareNext(Capsule.CapsuleType.UNIFORM));
    }

    static SidekickAttack injectExplosiveCapsule(Level lvl) {
        return new SidekickAttack(lvl, () -> lvl.prepareNext(Capsule.CapsuleType.EXPLOSIVE));
    }
}

public class GermAttack extends LevelAttack {
    private GermAttack(Level level, double speed, Stream<Runnable> stream) {
        super(level, speed, stream);
    }

    private GermAttack(Level level, double speed, Runnable... moves) {
        this(level, speed, Arrays.stream(moves));
    }

    private GermAttack(Level level, Runnable move) {
        this(level, 0, move);
    }

    public static GermAttack hitRandomAdjacent(Level lvl, Germ germ) {
        return new GermAttack(lvl, () -> Utils.getOptionalRandomFrom(lvl.getGrid().capsuleStack()
            .map(GridObject::coordinates)
            .filter(c -> Math.abs(germ.coordinates().x - c.x) <= 1 && Math.abs(germ.coordinates().y - c.y) <= 1))
          .ifPresent(c -> lvl.attack(c, AttackType.SLICE)));
    }

    public static GermAttack contaminateRandomCapsule(Level lvl) {
        return new GermAttack(lvl, () -> getRandomCapsule(lvl).ifPresent(caps -> {
            var virus = Germ.cooldownGermOfKind(Germ.GermKind.VIRUS, caps.color());
            virus.startAttacking();
            lvl.getGrid().replace(caps, virus);
            lvl.visualParticles().addContaminationEffect(virus);
        }));
    }

    public static GermAttack doNothing(Level lvl) {
        return new GermAttack(lvl, () -> {});
    }
}
