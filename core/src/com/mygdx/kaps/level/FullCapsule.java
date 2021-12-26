package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;

import java.util.function.Consumer;

class FullCapsule extends CapsulePart {
    private boolean frozen;
    private boolean falling;
    private final CapsulePart slave;

    private FullCapsule(Coordinates coordinates, Color color, Orientation orientation, CapsulePart slave) {
        super(coordinates, color, orientation);
        this.slave = slave;
    }

    private FullCapsule(Coordinates coordinates, Color mainColor, Color slaveColor, Orientation orientation) {
        this(
          coordinates, mainColor, orientation,
          new CapsulePart(coordinates.addedTo(orientation.oppositeVector()), slaveColor, orientation.opposite())
        );
    }

    static FullCapsule randomNewInstance(Level level) {
        return new FullCapsule(
          level.spawnCoordinates(),
          Utils.getRandomFrom(level.getColors()),
          Utils.getRandomFrom(level.getColors()),
          Orientation.LEFT
        );
    }

    FullCapsule copy() {
        return new FullCapsule(coordinates(), color(), orientation(), slave.copy());
    }

    void forEachCapsule(Consumer<CapsulePart> action) {
        action.accept(this);
        action.accept(slave);
    }

    boolean canStandIn(Grid grid) {
        return super.canStandIn(grid) && slave.canStandIn(grid);
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
        slave.face(this);
    }

    void dip() {
        super.dip();
        updateSlave();
    }

    void flip() {
        super.flip();
        updateSlave();
    }

    void moveLeft() {
        super.moveLeft();
        updateSlave();
    }

    void moveRight() {
        super.moveRight();
        updateSlave();
    }

    void moveForward() {
        super.moveForward();
        updateSlave();
    }

    /**
     * @param action the move to apply on the capsule
     * @return a copy of the current instance peeked by {@param action}
     */
    private FullCapsule shifted(Consumer<FullCapsule> action) {
        FullCapsule test = copy();
        action.accept(test);
        return test;
    }

    FullCapsule dipped() {
        return shifted(FullCapsule::dip);
    }

    FullCapsule flipped() {
        return shifted(FullCapsule::flip);
    }

    FullCapsule movedLeft() {
        return shifted(FullCapsule::moveLeft);
    }

    FullCapsule movedRight() {
        return shifted(FullCapsule::moveRight);
    }

    FullCapsule movedBack() {
        return shifted(FullCapsule::moveForward);
    }
}
