
package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.Arrays;
import java.util.function.Function;

public abstract class Germ extends GridObject {
    public enum GermKind {
        BASIC(.1f),
        WALL(.15f),
        THORN(.1f),
        VIRUS(.125f),
        ;

        private final float animationSpeed;

        GermKind(float speed) {
            animationSpeed = speed;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public float getAnimationSpeed() {
            return animationSpeed;
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

        private static Function<Color, Germ> getKindOfSymbol(char symbol) {
            return Arrays.stream(values())
              .filter(k -> (symbol) == k.toString().charAt(0))
              .findFirst()
              .map(k -> k.associatedGerm)
              .orElseThrow(() -> new IllegalArgumentException("Couldn't resolve germ with symbol: " + symbol));
        }
    }

    private final GermKind kind;

    Germ(Color color, GermKind kind, int mana) {
        super(new Coordinates(), color, mana);
        this.kind = kind;
    }

    Germ(Color color, GermKind kind) {
        this(color, kind, 2);
    }

    public static Germ ofSymbol(char symbol) {
        return GermSupplier.getKindOfSymbol(symbol).apply(Color.random());
    }

    public static Germ random(Coordinates coordinates) {
        var randomGerm = Utils.getRandomFrom(GermSupplier.values()).associatedGerm.apply(Color.random());
        randomGerm.coordinates().set(coordinates);
        return randomGerm;
    }

    @Override
    public boolean isGerm() {
        return true;
    }

    @Override
    public Sprite getSprite(SpriteData data) {
        return data.getGerm(kind, color()).getCurrentSprite();
    }

    @Override
    public AnimatedSprite poppingAnim() {
        return SpriteData.poppingGermAnimation(kind, color());
    }
}

final class BasicGerm extends Germ {
    BasicGerm(Color color) {
        super(color, GermKind.BASIC);
    }
}

final class WallGerm extends Germ {
    private static final int maxHealth = 4;
    private int health;

    WallGerm(Color color, int health) {
        super(color, GermKind.BASIC, 3);
        if (health <= 0 || maxHealth < health)
            throw new IllegalArgumentException("Invalid health: " + health + " / " + maxHealth);

        this.health = health;
    }

    WallGerm(Color color) {
        this(color, maxHealth);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void takeHit() {
        if (health > 0) health--;
    }

    @Override
    public Sprite getSprite(SpriteData data) {
        return data.getWallGerm(health, color()).getCurrentSprite();
    }

    @Override
    public AnimatedSprite poppingAnim() {
        return SpriteData.poppingWallAnimation(health, color());
    }
}

final class VirusGerm extends Germ {
    VirusGerm(Color color) {
        super(color, GermKind.VIRUS);
    }
}

final class ThornGerm extends Germ {
    ThornGerm(Color color) {
        super(color, GermKind.THORN);
    }
}
