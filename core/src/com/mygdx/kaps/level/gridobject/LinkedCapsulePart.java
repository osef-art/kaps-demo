package com.mygdx.kaps.level.gridobject;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LinkedCapsulePart extends CapsulePart {
    private Orientation orientation;
    private LinkedCapsulePart linked;

    public LinkedCapsulePart(Coordinates coordinates, Color color) {
        super(coordinates, color);
    }

    private LinkedCapsulePart(Coordinates coordinates, Color color, Orientation orientation) {
        this(coordinates, color);
        this.orientation = orientation;
    }

    public LinkedCapsulePart(Coordinates coordinates, Color color, Orientation side, LinkedCapsulePart linked) {
        this(coordinates, color);
        Objects.requireNonNull(linked);
        linkTo(linked, side.opposite());
    }

    public LinkedCapsulePart copy() {
        return new LinkedCapsulePart(coordinates(), color(), orientation);
    }

    public Optional<LinkedCapsulePart> linked() {
        return Optional.of(linked);
    }

    public Orientation orientation() {
        return orientation;
    }

    void linkTo(LinkedCapsulePart linked, Orientation side) {
        orientation = side.opposite();
        this.linked = linked;
        linked.linked = this;
        updateLinked();
    }

    public void updateLinked() {
        linked.face(this);
    }

    private Coordinates facingCoordinates() {
        return coordinates().addedTo(orientation.oppositeVector());
    }

    private void face(LinkedCapsulePart caps) {
        orientation = caps.orientation.opposite();
        coordinates().set(caps.facingCoordinates());
    }

    public void moveForward() {
        moveTowards(orientation);
    }

    public void flip() {
        orientation = orientation.flipped();
    }

    public boolean atLeastOneVerify(Predicate<CapsulePart> condition) {
        return condition.test(this) || condition.test(linked);
    }

    public boolean verify(Predicate<CapsulePart> condition) {
        return condition.test(this) && condition.test(linked);
    }

    public boolean verticalVerify(Predicate<CapsulePart> condition) {
        return orientation().isVertical() ? atLeastOneVerify(condition) : verify(condition);
    }

    public void applyForEach(Consumer<CapsulePart> action, Consumer<CapsulePart> linkedAction) {
        action.accept(this);
        linkedAction.accept(linked);
    }

    public void applyToBoth(Consumer<CapsulePart> action) {
        applyForEach(action, action);
    }
}
