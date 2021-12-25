package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;

import java.util.function.Consumer;
import java.util.function.Predicate;

class Gelule {
    private boolean frozen;
    private boolean falling;
    private final Capsule main;
    private final Capsule slave;

    private Gelule(Capsule main, Capsule slave) {
        this.main = main;
        this.slave = slave;
    }

    private Gelule(int mainX, int mainY, Color mainColor, Color slaveColor) {
        this(
          new Capsule(mainX, mainY, mainColor, Orientation.LEFT),
          new Capsule(mainX + 1, mainY, slaveColor, Orientation.RIGHT)
        );
    }

    static Gelule randomNewInstance(Level level) {
        return new Gelule(
          level.getGrid().getWidth() / 2 - 1,
          level.getGrid().getHeight() - 1,
          Utils.getRandomFrom(level.getColors()),
          Utils.getRandomFrom(level.getColors())
        );
    }

    private Gelule copy() {
        return new Gelule(main.copy(), slave.copy());
    }

    void forEachCapsule(Consumer<Capsule> action) {
        action.accept(main);
        action.accept(slave);
    }

    boolean bothVerify(Predicate<Capsule> condition) {
        return condition.test(main) && condition.test(slave);
    }

    boolean isInGrid(Grid grid) {
        return bothVerify(c -> c.isInGrid(grid));
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

    private void updateSlave() {
        slave.face(main);
    }

    /**
     * Applies an atomic move to the main capsule and update its slave.
     * @param action the move to apply on the main capsule
     */
    private void shift(Consumer<Capsule> action) {
        action.accept(main);
        updateSlave();
    }

    void dip() {
        shift(Capsule::dip);
    }

    void flip() {
        shift(Capsule::flip);
    }

    void moveLeft() {
        shift(Capsule::moveLeft);
    }

    void moveRight() {
        shift(Capsule::moveRight);
    }

    void moveBack() {
        shift(Capsule::moveInDirection);
    }

    /**
     * @param action the move to apply on the gelule
     * @return a copy of the current instance peeked by {@param action}
     */
    private Gelule shifted(Consumer<Gelule> action) {
        Gelule test = copy();
        action.accept(test);
        return test;
    }

    Gelule dipped() {
        return shifted(Gelule::dip);
    }

    Gelule flipped() {
        return shifted(Gelule::flip);
    }

    Gelule movedLeft() {
        return shifted(Gelule::moveLeft);
    }

    Gelule movedRight() {
        return shifted(Gelule::moveRight);
    }

    Gelule movedBack() {
        return shifted(Gelule::moveBack);
    }
}
