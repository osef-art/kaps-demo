package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.HashMap;

class CapsulePart extends GridObject {
    private final HashMap<Orientation, Sprite> sprites = new HashMap<>();
    private Orientation orientation;

    CapsulePart(Coordinates coordinates, Color color, Orientation orientation) {
        super(coordinates, color);
        this.orientation = orientation;
        Arrays.stream(Orientation.values()).forEach(o -> {
            var sprite = new Sprite(
              new Texture("android/assets/sprites/" + color.id() + "/caps/" + o + ".png")
            );
            sprite.flip(false, true);
            sprites.put(o, sprite);
        });
    }

    CapsulePart copy() {
        return new CapsulePart(coordinates(), color(), orientation);
    }

    Sprite getSprite() {
        return sprites.get(orientation);
    }

    Orientation orientation() {
        return orientation;
    }

    private Coordinates facingCoordinates() {
        return coordinates().addedTo(orientation.oppositeVector());
    }

    private boolean isInGrid(Grid grid) {
        return grid.isInGrid(coordinates());
    }

    private boolean overlapsStack(Grid grid) {
        return grid.get(coordinates()).isPresent();
    }

    boolean canStandIn(Grid grid) {
        return isInGrid(grid) && !overlapsStack(grid);
    }

    /**
     * Makes the instance move in the direction specified by {@param orientation}
     *
     * @param orientation the direction in which the movement is made
     */
    private void moveTowards(Orientation orientation) {
        coordinates().add(orientation.directionVector());
    }

    void moveForward() {
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

    void face(CapsulePart caps) {
        orientation = caps.orientation.opposite();
        coordinates().set(caps.facingCoordinates());
    }
}
