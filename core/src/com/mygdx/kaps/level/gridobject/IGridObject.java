package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;

public interface IGridObject {
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

    GridObject copy(Color color);
}
