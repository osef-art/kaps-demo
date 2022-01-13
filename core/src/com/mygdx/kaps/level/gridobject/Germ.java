
package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Germ extends GridObject {
    enum GermKind {
        BASIC(.1f),
        WALL(.15f),
        THORN(.125f),
        VIRUS(.15f),
        ;

        private final float animationSpeed;

        GermKind(float speed) {
            animationSpeed = speed;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    enum GermSupplier {
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

        public static Function<Color, Germ> getKindOfSymbol(char symbol) {
            return Arrays.stream(values())
              .filter(k -> (symbol) == k.toString().charAt(0))
              .findFirst()
              .map(k -> k.associatedGerm)
              .orElseThrow(() -> new IllegalArgumentException("Couldn't resolve germ with symbol: " + symbol));
        }
    }


    private final AnimatedSprite anim;

    Germ(Coordinates coordinates, Color color, GermKind kind) {
        super(coordinates, color, "android/assets/sprites/" + color.id() + "/germs/" + kind);
        anim = new AnimatedSprite(
          "android/assets/sprites/" + color.id() + "/germs/" + kind + "/idle_", 8, kind.animationSpeed
        );
    }

    Germ(Color color, GermKind kind) {
        this(new Coordinates(), color, kind);
    }

    public static Germ ofSymbol(char symbol, Set<Color> colors) {
        return GermSupplier.getKindOfSymbol(symbol).apply(Utils.getRandomFrom(colors));
    }

    public static Germ random(Coordinates coordinates, Color color) {
        return new BasicGerm(coordinates, color);
    }

    @Override
    public Sprite getSprite() {
        return anim.getCurrentSprite();
    }

    @Override
    public boolean isGerm() {
        return true;
    }

    @Override
    public void updateSprite() {
        anim.updateExistenceTime();
    }
}


final class BasicGerm extends Germ {
    public BasicGerm(Coordinates coordinates, Color color) {
        super(coordinates, color, GermKind.BASIC);
    }

    public BasicGerm(Color color) {
        super(color, GermKind.BASIC);
    }

    @Override
    public GridObject copy(Color color) {
        return new BasicGerm(coordinates(), color);
    }
}

final class WallGerm extends Germ {
    private static final int maxHealth = 4;
    private final List<AnimatedSprite> animations;
    private int health;

    public WallGerm(Coordinates coordinates, Color color, int health) {
        super(coordinates, color, GermKind.BASIC);
        if (health <= 0 || maxHealth < health)
            throw new IllegalArgumentException("Invalid health: " + health + " / " + maxHealth);

        this.health = health;
        animations = IntStream.range(0, maxHealth).mapToObj(n -> new AnimatedSprite(
          "android/assets/sprites/" + color.id() + "/germs/" + GermKind.WALL + (n + 1) + "/idle_",
          n > 1 ? 4 : 8,
          n > 1 ? .2f : .15f
        )).collect(Collectors.toList());

    }

    public WallGerm(Color color, int health) {
        this(new Coordinates(), color, health);
    }

    public WallGerm(Color color) {
        this(color, maxHealth);
    }

    @Override
    public GridObject copy(Color color) {
        return new WallGerm(coordinates(), color, health);
    }

    @Override
    public Sprite getSprite() {
        return animations.get(health - 1).getCurrentSprite();
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void takeHit() {
        if (health > 0) health--;
    }

    @Override
    public void updateSprite() {
        animations.forEach(AnimatedSprite::updateExistenceTime);
    }
}

final class VirusGerm extends Germ {
    public VirusGerm(Coordinates coordinates, Color color) {
        super(coordinates, color, GermKind.VIRUS);
    }

    public VirusGerm(Color color) {
        super(color, GermKind.VIRUS);
    }

    @Override
    public GridObject copy(Color color) {
        return new VirusGerm(coordinates(), color);
    }
}

final class ThornGerm extends Germ {
    public ThornGerm(Coordinates coordinates, Color color) {
        super(coordinates, color, GermKind.THORN);
    }

    public ThornGerm(Color color) {
        super(color, GermKind.THORN);
    }

    @Override
    public GridObject copy(Color color) {
        return new ThornGerm(coordinates(), color);
    }
}
