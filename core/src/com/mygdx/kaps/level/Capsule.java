package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.*;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

class Capsule {
    enum CapsuleType {UNIFORM, EXPLOSIVE}

    private final LinkedCapsulePart main;
    private Capsule preview;

    private Capsule(CapsulePart main, CapsulePart slave, Orientation mainOrientation) {
        var linked = new LinkedCapsulePart(Objects.requireNonNull(slave).coordinates(), slave.color(), slave.type());
        this.main = new LinkedCapsulePart(Objects.requireNonNull(main).coordinates(), main.color(), main.type(), mainOrientation, linked);
    }

    static Capsule buildRandomInstance(Coordinates coordinates, Set<Color> colors, Set<CapsuleType> types) {
        var mainColor = Utils.getRandomFrom(colors);
        var slaveColor = Utils.getRandomFrom(colors);
        var explosive = new Random().nextBoolean();

        var main = types.contains(CapsuleType.EXPLOSIVE) ?
                     CapsulePart.explosiveCapsule(coordinates, mainColor) :
                     new CapsulePart(coordinates, mainColor);
        var slave = new CapsulePart(coordinates, types.contains(CapsuleType.UNIFORM) ? mainColor : slaveColor);

        return new Capsule(
          explosive ? main : slave,
          explosive ? slave : main,
          Orientation.LEFT
        );
    }

    private Capsule copy() {
        return copy(main.coordinates(), main.orientation());
    }

    Capsule copy(Coordinates coordinates, Orientation orientation) {
        var copy = main.copy();
        copy.coordinates().set(coordinates);
        return new Capsule(copy, main.linked().map(LinkedCapsulePart::copy).orElse(null), orientation);
    }

    @Override
    public String toString() {
        return String.format("(%s | %s)", main, main.linked().orElse(main));
    }

    Optional<Capsule> preview() {
        return Optional.ofNullable(preview);
    }

    void computePreview(Grid grid) {
        preview = copy();
        while (preview.dipped().canStandIn(grid)) preview.dip();
    }

    void clearPreview() {
        preview = null;
    }

    boolean canStandIn(Grid grid) {
        return main.verify(grid::canBePut);
    }

    boolean atLeastOneVerify(Predicate<CapsulePart> condition) {
        return main.atLeastOneVerify(condition);
    }

    void applyForEach(Consumer<CapsulePart> mainAction, Consumer<CapsulePart> slaveAction) {
        main.applyForEach(mainAction, slaveAction);
    }

    void applyToBoth(Consumer<CapsulePart> action) {
        main.applyToBoth(action);
    }

    void startDropping() {
        applyToBoth(CapsulePart::initDropping);
        clearPreview();
    }

    /**
     * Applies an atomic move to the main capsule and update its slave.
     *
     * @param action the move to apply on the main capsule
     */
    private void shift(Consumer<LinkedCapsulePart> action) {
        action.accept(main);
        main.updateLinked();
    }

    void dip() {
        shift(LinkedCapsulePart::dip);
    }

    void flip() {
        shift(LinkedCapsulePart::flip);
    }

    void moveLeft() {
        shift(LinkedCapsulePart::moveLeft);
    }

    void moveRight() {
        shift(LinkedCapsulePart::moveRight);
    }

    void moveForward() {
        shift(LinkedCapsulePart::moveForward);
    }

    /**
     * @param action the move to apply on the capsule
     * @return a copy of the current instance peeked by {@param action}
     */
    private Capsule shifted(Consumer<Capsule> action) {
        Capsule test = copy();
        action.accept(test);
        return test;
    }

    Capsule dipped() {
        return shifted(Capsule::dip);
    }

    Capsule flipped() {
        return shifted(Capsule::flip);
    }

    Capsule movedLeft() {
        return shifted(Capsule::moveLeft);
    }

    Capsule movedRight() {
        return shifted(Capsule::moveRight);
    }

    Capsule movedBack() {
        return shifted(Capsule::moveForward);
    }
}
