package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.Grid;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CapsulePart extends GridObject {
    private final Sprite sprite;
    private boolean dropping;

    public CapsulePart(LinkedCapsulePart caps) {
        this(caps.coordinates(), caps.color());
    }

    CapsulePart(Coordinates coordinates, Color color) {
        super(coordinates, color, "android/assets/sprites/" + color.id() + "/caps");
        sprite = new Sprite(
          new Texture("android/assets/sprites/" + color.id() + "/caps/" + Orientation.NONE + ".png")
        );
    }

    CapsulePart copy() {
        return new CapsulePart(coordinates(), color());
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }

    public Optional<LinkedCapsulePart> linked() {
        return Optional.empty();
    }

    Orientation orientation() {
        return Orientation.NONE;
    }

    @Override
    public boolean isCapsule() {
        return true;
    }

    @Override
    public boolean isDropping() {
        return dropping;
    }

    public void initDropping() {
        dropping = true;
    }

    public void freeze() {
        dropping = false;
    }

    private boolean isInGridBounds(Grid grid) {
        return grid.isInGridBounds(coordinates());
    }

    private boolean overlapsStack(Grid grid) {
        return grid.get(coordinates()).isPresent();
    }

    public boolean canStandIn(Grid grid) {
        return isInGridBounds(grid) && !overlapsStack(grid);
    }

    boolean verify(Predicate<CapsulePart> condition) {
        return condition.test(this);
    }

    public boolean verticalVerify(Predicate<CapsulePart> condition) {
        return condition.test(this);
    }

    public void applyToBoth(Consumer<CapsulePart> action) {
        action.accept(this);
    }

    /**
     * Makes the instance move in the direction specified by {@param orientation}
     *
     * @param orientation the direction in which the movement is made
     */
    void moveTowards(Orientation orientation) {
        coordinates().add(orientation.directionVector());
    }

    void moveLeft() {
        moveTowards(Orientation.LEFT);
    }

    void moveRight() {
        moveTowards(Orientation.RIGHT);
    }

    public void dip() {
        moveTowards(Orientation.DOWN);
    }

    public CapsulePart dipped() {
        var test = copy();
        test.dip();
        return test;
    }
}

