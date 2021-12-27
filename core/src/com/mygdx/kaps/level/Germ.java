package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.renderer.SpriteSet;

public class Germ extends GridObject {
    private final SpriteSet sprites;

    Germ(Coordinates coordinates, Color color) {
        super(coordinates, color);
        sprites = new SpriteSet("android/assets/sprites/" + color.id() + "/germs/basic/idle_", 8, 0);
    }

    @Override
    public Sprite getSprite() {
        return sprites.getCurrentSprite();
    }

    @Override
    public boolean isGerm() {
        return true;
    }

    @Override
    public boolean isCapsule() {
        return false;
    }
}
