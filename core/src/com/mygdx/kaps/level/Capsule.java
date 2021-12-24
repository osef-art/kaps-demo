package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.HashMap;

public class Capsule {
    static class Position {
        int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    private final HashMap<Orientation, Sprite> sprites = new HashMap<>();
    private Orientation orientation;
    private final Position position;
    private final Color color;

    public Capsule(int x, int y, Color color, Orientation orientation) {
        position = new Position(x, y);
        this.orientation = orientation;
        this.color = color;
        Arrays.stream(Orientation.values()).forEach(o -> sprites.put(o, new Sprite(
          new Texture("android/assets/sprites/" + color.id() + "/caps/" + o + ".png")
        )));
    }

    public void flip() {
        orientation = orientation.flipped();
    }

    public Sprite getSprite() {
        return sprites.get(orientation);
    }

    public Position getPosition() {
        return position;
    }
}
