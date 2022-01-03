package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Objects;

interface IGridObject {
    boolean isGerm();

    boolean isCapsule();

    boolean isDropping();

    boolean isDestroyed();

    boolean hasVanished();

    void pop();

    void takeHit();

    void updateSprite();

    void updatePoppingSprite();

    Sprite getSprite();

    Sprite getPoppingSprite();
}

abstract class GridObject implements IGridObject {
    private final AnimatedSprite poppingAnim;
    private final Coordinates coordinates;
    private final Color color;
    private boolean destroyed;

    GridObject(Coordinates coordinates, Color color, String path) {
        this.color = color;
        this.coordinates = Objects.requireNonNull(coordinates).copy();
        poppingAnim = new AnimatedSprite(path + "/pop_", 8, 0.075f);
    }

    @Override
    public String toString() {
        return coordinates.toString();
    }

    Coordinates coordinates() {
        return coordinates;
    }

    Color color() {
        return color;
    }

    public boolean isCapsule() {
        return false;
    }

    public boolean isGerm() {
        return false;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isDropping() {
        return false;
    }

    @Override
    public boolean hasVanished() {
        return poppingAnim.isFinished();
    }

    public Sprite getPoppingSprite() {
        return poppingAnim.getCurrentSprite();
    }

    public void takeHit() {
        destroyed = true;
    }

    public void updateSprite() {
    }

    public void pop() {
        poppingAnim.reset();
    }

    public void updatePoppingSprite() {
        poppingAnim.updateExistenceTime();
    }
}
