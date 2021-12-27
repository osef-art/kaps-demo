package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;

import java.util.function.Consumer;
import java.util.function.Predicate;

class Capsule {
    private final LinkedCapsulePart main;
    private final LinkedCapsulePart slave;

    private Capsule(CapsulePart main, CapsulePart slave, Orientation mainOrientation) {
        this.slave = new LinkedCapsulePart(slave.coordinates(), slave.color());
        this.main = new LinkedCapsulePart(main.coordinates(), main.color(), mainOrientation, this.slave);
    }

    private Capsule(LinkedCapsulePart main, LinkedCapsulePart slave) {
        this(main, slave, main.orientation());
    }

    private Capsule(Coordinates coordinates, Color mainColor, Color slaveColor) {
        this(
          new CapsulePart(coordinates, mainColor),
          new CapsulePart(coordinates, slaveColor),
          Orientation.LEFT
        );
    }

    static Capsule randomNewInstance(Level level) {
        return new Capsule(
          level.spawnCoordinates(),
          Utils.getRandomFrom(level.getColors()),
          Utils.getRandomFrom(level.getColors())
        );
    }

    Capsule copy() {
        return new Capsule(main.copy(), slave.copy());
    }

    @Override
    public String toString() {
        return "(" + main + " | " + slave + ")";
    }

    private boolean bothVerify(Predicate<LinkedCapsulePart> condition) {
        return condition.test(main) && condition.test(slave);
    }

    boolean canStandIn(Grid grid) {
        return bothVerify(p -> p.canStandIn(grid));
    }

    boolean isDropping() {
        return bothVerify(LinkedCapsulePart::isDropping);
    }

    boolean isFrozen() {
        return bothVerify(LinkedCapsulePart::isFrozen);
    }

    void forEachPart(Consumer<LinkedCapsulePart> action) {
        action.accept(main);
        action.accept(slave);
    }

    void startDropping() {
        forEachPart(LinkedCapsulePart::startDropping);
    }

    void freeze() {
        forEachPart(LinkedCapsulePart::freeze);
    }

    private void updateSlave() {
        slave.face(main);
    }

    /**
     * Applies an atomic move to the main capsule and update its slave.
     * @param action the move to apply on the main capsule
     */
    private void shift(Consumer<LinkedCapsulePart> action) {
        action.accept(main);
        updateSlave();
    }

    void dip() {
        shift(LinkedCapsulePart::dip);
    }

    void flip() {
        shift(LinkedCapsulePart::flip);
    }

    void moveLeft() {
        shift(LinkedCapsulePart::moveLeft);
    }

    void moveRight() {
        shift(LinkedCapsulePart::moveRight);
    }

    void moveForward() {
        shift(LinkedCapsulePart::moveForward);
    }

    /**
     * @param action the move to apply on the capsule
     * @return a copy of the current instance peeked by {@param action}
     */
    private Capsule shifted(Consumer<Capsule> action) {
        Capsule test = copy();
        action.accept(test);
        return test;
    }

    Capsule dipped() {
        return shifted(Capsule::dip);
    }

    Capsule flipped() {
        return shifted(Capsule::flip);
    }

    Capsule movedLeft() {
        return shifted(Capsule::moveLeft);
    }

    Capsule movedRight() {
        return shifted(Capsule::moveRight);
    }

    Capsule movedBack() {
        return shifted(Capsule::moveForward);
    }
}
