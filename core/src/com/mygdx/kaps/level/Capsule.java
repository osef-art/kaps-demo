package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;

import java.util.function.Consumer;

class Capsule {
    private boolean frozen;
    private boolean falling;
    private final CapsulePart main;
    private final CapsulePart slave;

    private Capsule(CapsulePart main, CapsulePart slave) {
        this.main = main;
        this.slave = slave;
    }

    private Capsule(Coordinates coordinates, Color mainColor, Color slaveColor) {
        this(
          new CapsulePart(coordinates, mainColor),
          new CapsulePart(coordinates, slaveColor)
        );
        main.linkTo(slave, Orientation.RIGHT);
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

    boolean canStandIn(Grid grid) {
        return main.canStandIn(grid) && slave.canStandIn(grid);
    }

    boolean isFalling() {
        return falling;
    }

    boolean isFrozen() {
        return frozen;
    }

    void startFalling() {
        falling = true;
    }

    void freeze() {
        frozen = true;
    }

    void forEachPart(Consumer<CapsulePart> action) {
        action.accept(main);
        action.accept(slave);
    }

    private void updateSlave() {
        slave.face(main);
    }

    /**
     * Applies an atomic move to the main capsule and update its slave.
     * @param action the move to apply on the main capsule
     */
    private void shift(Consumer<CapsulePart> action) {
        action.accept(main);
        updateSlave();
    }

    void dip() {
        shift(CapsulePart::dip);
    }

    void flip() {
        shift(CapsulePart::flip);
    }

    void moveLeft() {
        shift(CapsulePart::moveLeft);
    }

    void moveRight() {
        shift(CapsulePart::moveRight);
    }

    void moveForward() {
        shift(CapsulePart::moveForward);
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
