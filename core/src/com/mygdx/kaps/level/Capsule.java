package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.HashMap;

public class Capsule {
    private final HashMap<Orientation, Sprite> sprites = new HashMap<>();
    private Orientation orientation;
    private final Coordinates coordinates;
    private final Color color;

    public Capsule(int x, int y, Color color, Orientation orientation) {
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

    public void moveLeft() {
        coordinates.add(Orientation.LEFT.movingVector());
    }

    public void moveRight() {
        coordinates.add(Orientation.RIGHT.movingVector());
    }

    public void flip() {
        orientation = orientation.flipped();
    }

    public void dip() {
        coordinates.add(Orientation.DOWN.movingVector());
    }

    public Sprite getSprite() {
        return sprites.get(orientation);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    private Coordinates facingCoordinates() {
        return coordinates.addedTo(orientation.oppositeVector());
    }

    public void face(Capsule caps) {
        orientation = caps.orientation.facing();
        coordinates.set(caps.facingCoordinates());
    }
}
