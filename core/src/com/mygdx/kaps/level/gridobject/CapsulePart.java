package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.AttackType;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class CapsulePart extends GridObject {
    public enum BonusType {
        NONE("", (lvl, caps) -> {}),
        EXPLOSIVE("bomb", (lvl, caps) -> {
            IntStream.rangeClosed(-1, 1).forEach(x -> IntStream.rangeClosed(-1, 1).forEach(y -> {
                if (x != 0 || y != 0) lvl.attack(caps.coordinates().addedTo(x, y), AttackType.FIRE);
            }));
        });

        private final String path;
        private final BiConsumer<Level, CapsulePart> effect;

        BonusType(String path, BiConsumer<Level, CapsulePart> effect) {
            this.path = path;
            this.effect = effect;
        }

        public String getPath() {
            return path;
        }
    }

    private final BonusType type;
    private boolean dropping;

    CapsulePart(Coordinates coordinates, Color color, BonusType type) {
        super(coordinates, color);
        this.type = type;
    }

    public CapsulePart(Coordinates coordinates, Color color) {
        this(coordinates, color, BonusType.NONE);
    }

    public CapsulePart(LinkedCapsulePart caps) {
        this(caps.coordinates(), caps.color(), caps.type());
    }

    public static CapsulePart explosiveCapsule(Coordinates coordinates, Color color) {
        return new CapsulePart(coordinates, color, BonusType.EXPLOSIVE);
    }

    public CapsulePart dipped() {
        var test = new CapsulePart(coordinates(), color(), type);
        test.dip();
        return test;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Sprite getSprite(SpriteData spriteData) {
        return spriteData.getCapsule(orientation(), color(), type);
    }

    public Optional<LinkedCapsulePart> linked() {
        return Optional.empty();
    }

    Orientation orientation() {
        return Orientation.NONE;
    }

    public BonusType type() {
        return type;
    }

    @Override
    public boolean isCapsule() {
        return true;
    }

    @Override
    public boolean isDropping() {
        return dropping;
    }

    @Override
    public void ifGermElse(Consumer<Germ> germAction, Consumer<CapsulePart> capsAction) {
        capsAction.accept(this);
    }

    public void initDropping() {
        dropping = true;
    }

    public void freeze() {
        dropping = false;
    }

    public void triggerEffect(Level level) {
        type.effect.accept(level, this);
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

