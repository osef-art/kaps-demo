package com.mygdx.kaps.level;

public enum Orientation {
    NONE("unlinked"), LEFT("left"), UP("up"), RIGHT("right"), DOWN("down");

    private final String path;

    Orientation(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }

    public Orientation flipped() {
        switch (this) {
            case LEFT:
                return UP;
            case UP:
                return RIGHT;
            case RIGHT:
                return DOWN;
            case DOWN:
                return LEFT;
            default:
                return NONE;
        }
    }
}
