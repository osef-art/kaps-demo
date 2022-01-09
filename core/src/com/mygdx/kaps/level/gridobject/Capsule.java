package com.mygdx.kaps.level.gridobject;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.Grid;
import com.mygdx.kaps.level.Level;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Capsule {
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

    public static Capsule randomNewInstance(Level level) {
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

    public Optional<Capsule> preview() {
        return Optional.ofNullable(preview);
    }

    public void updatePreview(Grid grid) {
        preview = copy();
        while (preview.dipped().canStandIn(grid)) preview.dip();
    }

    public void clearPreview() {
        preview = null;
    }

    public boolean canStandIn(Grid grid) {
        return main.verify(p -> p.canStandIn(grid));
    }

    public boolean isDropping() {
        return main.verify(CapsulePart::isDropping);
    }

    boolean isFrozen() {
        return main.verify(Predicate.not(CapsulePart::isDropping));
    }

    public void applyForEach(Consumer<CapsulePart> mainAction, Consumer<CapsulePart> slaveAction) {
        main.applyForEach(mainAction, slaveAction);
    }

    public void applyToBoth(Consumer<CapsulePart> action) {
        main.applyToBoth(action);
    }

    public void startDropping() {
        applyToBoth(CapsulePart::initDropping);
        clearPreview();
    }

    void freeze() {
        applyToBoth(CapsulePart::freeze);
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

    public void dip() {
        shift(LinkedCapsulePart::dip);
    }

    public void flip() {
        shift(LinkedCapsulePart::flip);
    }

    public void moveLeft() {
        shift(LinkedCapsulePart::moveLeft);
    }

    public void moveRight() {
        shift(LinkedCapsulePart::moveRight);
    }

    public void moveForward() {
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

    public Capsule dipped() {
        return shifted(Capsule::dip);
    }

    public Capsule flipped() {
        return shifted(Capsule::flip);
    }

    public Capsule movedLeft() {
        return shifted(Capsule::moveLeft);
    }

    public Capsule movedRight() {
        return shifted(Capsule::moveRight);
    }

    public Capsule movedBack() {
        return shifted(Capsule::moveForward);
    }
}
