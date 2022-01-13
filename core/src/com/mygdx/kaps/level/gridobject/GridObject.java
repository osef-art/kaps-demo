package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Objects;

public abstract class GridObject implements IGridObject {
    private final AnimatedSprite poppingAnim;
    private final Coordinates coordinates;
    private boolean destroyed;
    private Color color;

    GridObject(Coordinates coordinates, Color color, String path) {
        this.color = color;
        this.coordinates = Objects.requireNonNull(coordinates).copy();
        poppingAnim = new AnimatedSprite(path + "/pop_", 8, 0.075f);
    }

    @Override
    public String toString() {
        return coordinates.toString();
    }

    public Coordinates coordinates() {
        return coordinates;
    }

    public Color color() {
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

    public void repaint(Color color) {
        this.color = color;
    }
}
