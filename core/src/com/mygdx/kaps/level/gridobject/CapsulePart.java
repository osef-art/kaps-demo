package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
public class CapsulePart extends GridObject {
    private boolean dropping;

    public CapsulePart(LinkedCapsulePart caps) {
        this(caps.coordinates(), caps.color());
    }

    public CapsulePart(Coordinates coordinates, Color color) {
        super(coordinates, color);
    }

    public CapsulePart dipped() {
        var test = new CapsulePart(coordinates(), color());
        test.dip();
        return test;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Sprite getSprite(SpriteData spriteData) {
        return spriteData.getCapsule(orientation(), color());
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

    public void moveLeft() {
        moveTowards(Orientation.LEFT);
    }

    public void moveRight() {
        moveTowards(Orientation.RIGHT);
    }

    public void dip() {
        moveTowards(Orientation.DOWN);
    }
}

