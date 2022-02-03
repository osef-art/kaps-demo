
package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.AttackType;
import com.mygdx.kaps.level.GermAttack;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Germ extends GridObject {
    public enum GermKind {
        BASIC(.1f),
        WALL(.15f),
        VIRUS(.125f, 8, AttackType.MAGIC, (lvl, g) -> GermAttack.contaminateRandomCapsule(lvl)),
        THORN(.1f, 5, AttackType.SLICE, GermAttack::hitRandomAdjacent),
        ;

        private final float animationSpeed;
        private final BiFunction<Level, Germ, GermAttack> attack;
        private final AttackType attackType;
        private final int cooldown;

        GermKind(float speed, int cooldown, AttackType type, BiFunction<Level, Germ, GermAttack> attack) {
            animationSpeed = speed;
            this.cooldown = cooldown;
            this.attack = attack;
            attackType = type;
        }

        GermKind(float speed) {
            this(speed, 0, AttackType.MELEE, (lvl, g) -> GermAttack.doNothing(lvl));
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public float getAnimationSpeed() {
            return animationSpeed;
        }

        int getCooldown() {
            return cooldown;
        }

        GermAttack newAttack(Level level, Germ germ) {
            return attack.apply(level, germ);
        }

        AttackType getAttackType() {
            return attackType;
        }
    }

    private enum GermSupplier {
        B(BasicGerm::new),
        W(c -> new WallGerm(c, 2)),
        X(c -> new WallGerm(c, 3)),
        Y(WallGerm::new),
        T(ThornGerm::new),
        V(VirusGerm::new),
        ;

        private final Function<Color, Germ> associatedGerm;

        GermSupplier(Function<Color, Germ> supplier) {
            associatedGerm = supplier;
        }

        private static Function<Color, Germ> getGermOfSymbol(char symbol) {
            return Arrays.stream(values())
              .filter(k -> (symbol) == k.toString().charAt(0))
              .findFirst()
              .map(k -> k.associatedGerm)
              .orElseThrow(() -> new IllegalArgumentException("Couldn't resolve germ with symbol: " + symbol));
        }
    }

    final GermKind kind;

    Germ(Color color, GermKind kind, int mana, int score) {
        super(new Coordinates(), color, mana, score);
        this.kind = kind;
    }

    Germ(Color color, GermKind kind) {
        this(color, kind, 2, 25);
    }

    public static Germ ofSymbol(char symbol) {
        return GermSupplier.getGermOfSymbol(symbol).apply(Color.random());
    }

    public static CooldownGerm cooldownGermOfKind(GermKind kind, Color color) {
        return Arrays.stream(GermSupplier.values())
          .map(k -> k.associatedGerm.apply(color))
          .filter(Germ::hasCooldown)
          .map(g -> (CooldownGerm) g)
          .filter(g -> g.isOfKind(kind))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Couldn't resolve germ of kind: " + kind));
    }

    public static Germ random(Coordinates coordinates) {
        var randomGerm = Utils.getRandomFrom(GermSupplier.values()).associatedGerm.apply(Color.random());
        randomGerm.coordinates().set(coordinates);
        return randomGerm;
    }

    public boolean isOfKind(GermKind kind) {
        return kind == this.kind;
    }

    public boolean hasCooldown() {
        return false;
    }

    @Override
    public boolean isGerm() {
        return true;
    }

    public GermKind kind() {
        return kind;
    }

    @Override
    public Sprite getSprite(SpriteData data) {
        return data.getGerm(kind, color()).getCurrentSprite();
    }

    @Override
    public AnimatedSprite poppingAnim() {
        return SpriteData.poppingGermAnimation(kind, color());
    }

    public void ifWall(Consumer<WallGerm> action) {
    }

    @Override
    public void ifGermElse(Consumer<Germ> germAction, Consumer<CapsulePart> capsAction) {
        germAction.accept(this);
    }

    public void ifHasCooldownElse(Consumer<CooldownGerm> cooldownAction, Consumer<Germ> germAction) {
        germAction.accept(this);
    }
}

final class BasicGerm extends Germ {
    BasicGerm(Color color) {
        super(color, GermKind.BASIC);
    }
}

