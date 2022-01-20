package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
 class Capsule {
    private final LinkedCapsulePart main;
    private Capsule preview;

    private Capsule(CapsulePart main, CapsulePart slave, Orientation mainOrientation) {
        var linked = new LinkedCapsulePart(slave.coordinates(), slave.color());
        this.main = new LinkedCapsulePart(main.coordinates(), main.color(), mainOrientation, linked);
    }

    private Capsule(LinkedCapsulePart main, LinkedCapsulePart slave) {
        this(main, slave, main.orientation());
    }

    private Capsule(Coordinates coordinates, Color mainColor, Color slaveColor) {
        this(
          new CapsulePart(coordinates, mainColor),
          new CapsulePart(coordinates, slaveColor),
          Orientation.LEFT
        );
    }

    static Capsule randomNewInstance(Level level) {
        return new Capsule(
          level.spawningCoordinates(),
          Utils.getRandomFrom(level.getColorSet()),
          Utils.getRandomFrom(level.getColorSet())
        );
    }

    static Capsule randomMonoColorInstance(Level level) {
        var color = Utils.getRandomFrom(level.getColorSet());
        return new Capsule(level.spawningCoordinates(), color, color);
    }

    Capsule copy() {
        return new Capsule(main.copy(), main.linked().map(LinkedCapsulePart::copy).orElse(null));
    }

    @Override
    public String toString() {
        return "(" + main + " | " + main.linked().orElse(main) + ")";
    }

    Optional<Capsule> preview() {
        return Optional.ofNullable(preview);
    }

    void updatePreview(Grid grid) {
        preview = copy();
        // TODO: replace by timer dipping preview really fast
        while (preview.dipped().canStandIn(grid)) preview.dip();
    }

    void clearPreview() {
        preview = null;
    }

    boolean canStandIn(Grid grid) {
        return main.verify(grid::canBePut);
    }

    boolean isDropping() {
        return main.verify(CapsulePart::isDropping);
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
