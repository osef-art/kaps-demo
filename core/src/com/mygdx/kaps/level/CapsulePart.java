package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

class CapsulePart extends GridObject {
    private final Sprite sprite;
    private boolean dropping;
    private boolean frozen;

    CapsulePart(LinkedCapsulePart caps) {
        this(caps.coordinates(), caps.color());
    }

    CapsulePart(Coordinates coordinates, Color color) {
        super(coordinates, color);
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

    boolean isDropping() {
        return dropping;
    }

    boolean isFrozen() {
        return frozen;
    }

    void startDropping() {
        dropping = true;
    }

    void stopDropping() {
        dropping = false;
    }

    void freeze() {
        frozen = true;
        stopDropping();
    }

    private boolean isInGridBounds(Grid grid) {
        return grid.isInGridBounds(coordinates());
    }

    private boolean overlapsStack(Grid grid) {
        return grid.get(coordinates()).isPresent();
    }

    boolean canStandIn(Grid grid) {
        return isInGridBounds(grid) && !overlapsStack(grid);
    }

    boolean atLeastOneVerify(Predicate<CapsulePart> condition) {
        return condition.test(this);
    }

    boolean verify(Predicate<CapsulePart> condition) {
        return condition.test(this);
    }

    void applyToBoth(Consumer<CapsulePart> action) {
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

    void dip() {
        moveTowards(Orientation.DOWN);
    }
}

class LinkedCapsulePart extends CapsulePart {
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

    @Override
    boolean atLeastOneVerify(Predicate<CapsulePart> condition) {
        return condition.test(this) || condition.test(linked);
    }

    boolean verify(Predicate<CapsulePart> condition) {
        return condition.test(this) && condition.test(linked);
    }

    void applyForEach(Consumer<CapsulePart> action, Consumer<CapsulePart> linkedAction) {
        action.accept(this);
        linkedAction.accept(linked);
    }

    void applyToBoth(Consumer<CapsulePart> action) {
        applyForEach(action, action);
    }
}