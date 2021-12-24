package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;

public class Gelule {
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

    public static Gelule randomNewInstance(Level level) {
        return new Gelule(
          level.getGrid().getDimensions().x / 2 - 1,
          level.getGrid().getDimensions().y - 1,
          Utils.getRandomFrom(level.getColors()),
          Utils.getRandomFrom(level.getColors())
        );
    }

    public Capsule getMainCapsule() {
        return main;
    }

    public Capsule getSlaveCapsule() {
        return slave;
    }
}
