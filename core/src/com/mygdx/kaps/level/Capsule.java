package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.HashMap;

class Capsule {
    private final HashMap<Orientation, Sprite> sprites = new HashMap<>();
    private final Coordinates coordinates;
    private Orientation orientation;
    private final Color color;

    Capsule(int x, int y, Color color, Orientation orientation) {
        coordinates = new Coordinates(x, y);
        this.orientation = orientation;
        this.color = color;
        Arrays.stream(Orientation.values()).forEach(o -> {
            var sprite = new Sprite(
              new Texture("android/assets/sprites/" + color.id() + "/caps/" + o + ".png")
            );
            sprite.flip(false, true);
            sprites.put(o, sprite);
        });
    }

    Capsule copy() {
        return new Capsule(coordinates.x, coordinates.y, color, orientation);
    }

    Sprite getSprite() {
        return sprites.get(orientation);
    }

    Coordinates getCoordinates() {
        return coordinates;
    }

    private Coordinates facingCoordinates() {
        return coordinates.addedTo(orientation.oppositeVector());
    }

    boolean isInGrid(Grid grid) {
        return 0 <= coordinates.x && coordinates.x < grid.getWidth() &&
                 0 <= coordinates.y && coordinates.y < grid.getHeight();
    }

    /**
     * Makes the instance move in the direction specified by {@param orientation}
     * @param orientation the direction in which the movement is made
     */
    private void moveTowards(Orientation orientation) {
        coordinates.add(orientation.movingVector());
    }

    void moveInDirection() {
        moveTowards(orientation);
    }

    void moveLeft() {
        moveTowards(Orientation.LEFT);
    }

    void moveRight() {
        moveTowards(Orientation.RIGHT);
    }

    void dip() {
        moveTowards(Orientation.DOWN);
    }

    void flip() {
        orientation = orientation.flipped();
    }

    void face(Capsule caps) {
        orientation = caps.orientation.facing();
        coordinates.set(caps.facingCoordinates());
    }
}
