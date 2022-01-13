package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LinkedCapsulePart extends CapsulePart {
    private final HashMap<Orientation, Sprite> sprites = new HashMap<>();
    private Orientation orientation;
    private LinkedCapsulePart linked;

    LinkedCapsulePart(Coordinates coordinates, Color color) {
        super(coordinates, color);
        Arrays.stream(Orientation.values())
          .forEach(o -> {
              var sprite = new Sprite(
                new Texture("android/assets/sprites/" + color.id() + "/caps/" + o + ".png")
              );
              sprite.flip(false, true);
              sprites.put(o, sprite);
          });
        sprites.put(Orientation.NONE, super.getSprite());
    }

    private LinkedCapsulePart(Coordinates coordinates, Color color, Orientation orientation) {
        this(coordinates, color);
        this.orientation = orientation;
    }

    LinkedCapsulePart(Coordinates coordinates, Color color, Orientation side, LinkedCapsulePart linked) {
        this(coordinates, color);
        Objects.requireNonNull(linked);
        linkTo(linked, side.opposite());
    }

    LinkedCapsulePart copy() {
        return new LinkedCapsulePart(coordinates(), color(), orientation);
    }

    public GridObject copy(Color color) {
        return new LinkedCapsulePart(coordinates(), color, orientation);
    }

    public Optional<LinkedCapsulePart> linked() {
        return Optional.of(linked);
    }

    Orientation orientation() {
        return orientation;
    }

    public Sprite getSprite() {
        return sprites.get(orientation);
    }

    void linkTo(LinkedCapsulePart linked, Orientation side) {
        orientation = side.opposite();
        this.linked = linked;
        linked.linked = this;
        updateLinked();
    }

    void updateLinked() {
        linked.face(this);
    }

    private Coordinates facingCoordinates() {
        return coordinates().addedTo(orientation.oppositeVector());
    }

    private void face(LinkedCapsulePart caps) {
        orientation = caps.orientation.opposite();
        coordinates().set(caps.facingCoordinates());
    }

    void moveForward() {
        moveTowards(orientation);
    }

    void flip() {
        orientation = orientation.flipped();
    }

    private boolean atLeastOneVerify(Predicate<CapsulePart> condition) {
        return condition.test(this) || condition.test(linked);
    }

    boolean verify(Predicate<CapsulePart> condition) {
        return condition.test(this) && condition.test(linked);
    }

    public boolean verticalVerify(Predicate<CapsulePart> condition) {
        return orientation().isVertical() ? atLeastOneVerify(condition) : verify(condition);
    }

    void applyForEach(Consumer<CapsulePart> action, Consumer<CapsulePart> linkedAction) {
        action.accept(this);
        linkedAction.accept(linked);
    }

    public void applyToBoth(Consumer<CapsulePart> action) {
        applyForEach(action, action);
    }
}
