package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Objects;
import java.util.function.Consumer;

interface IGridObject {
    default boolean isGerm() {
        return false;
    }

    default boolean isCapsule() {
        return false;
    }

    default boolean isDropping() {
        return false;
    }

    boolean isDestroyed();

    int manaWorth();

    Sprite getSprite(SpriteData data);

    void takeHit();

    void repaint(Color color);

    default void ifGerm(Consumer<Germ> action) {
        ifGermElse(action, c->{});
    }

    void ifGermElse(Consumer<Germ> germAction, Consumer<CapsulePart> capsAction);

    default void triggerEffect(Level level) {}
}

public abstract class GridObject implements IGridObject {
    private final Coordinates coordinates;
    private final int score;
    private final int mana;
    private boolean destroyed;
    private Color color;

    GridObject(Coordinates coordinates, Color color, int mana, int score) {
        this.coordinates = Objects.requireNonNull(coordinates).copy();
        this.color = color;
        this.score = score;
        this.mana = mana;
    }

    GridObject(Coordinates coordinates, Color color) {
        this(coordinates, color, 1, 10);
    }

    @Override
    public String toString() {
        return coordinates.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridObject)) return false;
        GridObject that = (GridObject) o;
        return coordinates.equals(that.coordinates) && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, color);
    }

    public Coordinates coordinates() {
        return coordinates;
    }

    public Color color() {
        return color;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public int manaWorth() {
        return mana;
    }

    public int getScore() {
        return score;
    }

    public void takeHit() {
        destroyed = true;
    }

    public void repaint(Color color) {
        this.color = color;
    }

    public AnimatedSprite poppingAnim() {
        return SpriteData.poppingAnimation(color);
    }
}
