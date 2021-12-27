package com.mygdx.kaps.level;

@SuppressWarnings("DuplicatedCode")
enum Orientation {
    NONE("unlinked"), LEFT("left"), UP("up"), RIGHT("right"), DOWN("down");

    private final String path;

    Orientation(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }

     boolean isVertical() {
        return this == UP || this == DOWN;
     }

     boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
     }

     Orientation flipped() {
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

     Orientation opposite() {
        switch (this) {
            case LEFT:
                return RIGHT;
            case UP:
                return DOWN;
            case RIGHT:
                return LEFT;
            case DOWN:
                return UP;
            default:
                return NONE;
        }
    }

     Coordinates directionVector() {
        switch (this) {
            case LEFT:
                return new Coordinates(-1, 0);
            case UP:
                return new Coordinates(0, 1);
            case RIGHT:
                return new Coordinates(1, 0);
            case DOWN:
                return new Coordinates(0, -1);
            default:
                return new Coordinates(0, 0);
        }
    }

     Coordinates oppositeVector() {
        return directionVector().mapped(x -> -x);
    }
}
