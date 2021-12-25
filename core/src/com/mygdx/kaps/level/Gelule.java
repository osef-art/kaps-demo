package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Gelule {
    private final Capsule main;
    private final Capsule slave;
    private boolean frozen;
    private boolean falling;

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

    public static Gelule randomNewInstance(Level level) {
        return new Gelule(
          level.getGrid().getWidth() / 2 - 1,
          level.getGrid().getHeight() - 1,
          Utils.getRandomFrom(level.getColors()),
          Utils.getRandomFrom(level.getColors())
        );
    }

    public boolean atLeastOneVerify(Predicate<Capsule> condition) {
        return condition.test(main) || condition.test(slave);
    }

    public boolean bothVerify(Predicate<Capsule> condition) {
        return condition.test(main) && condition.test(slave);
    }

    public void freeze() {
        frozen = true;
    }

    public void startFalling() {
        falling = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean isFalling() {
        return falling;
    }

    public Capsule getMainCapsule() {
        return main;
    }

    public Capsule getSlaveCapsule() {
        return slave;
    }

    private void updateSlave() {
        slave.face(main);
    }

    public void dip() {
        main.dip();
        updateSlave();
    }

    public void flip() {
        main.flip();
        updateSlave();
    }

    public void moveLeft() {
        main.moveLeft();
        updateSlave();
    }

    public void moveRight() {
        main.moveRight();
        updateSlave();
    }

    public void forEachCapsule(Consumer<Capsule> action) {
        action.accept(main);
        action.accept(slave);
    }
}
