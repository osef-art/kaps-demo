package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.renderer.SpriteSet;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

public abstract class Germ extends GridObject {
    enum GermKind {
        BASIC,
        WALL(4),
        THORN,
        VIRUS;

        private final int maxHealth;

        GermKind(int max) {
            maxHealth = max;
        }

        GermKind() {
            this(1);
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

    private final SpriteSet sprites;
    private final int maxHealth;
    private int health;

    private Germ(Coordinates coordinates, Color color, GermKind kind, int health) {
        super(coordinates, color);
        if (health <= 0 || kind.maxHealth < health)
            throw new IllegalArgumentException("Invalid health: " + health + " / " + kind.maxHealth);
        maxHealth = kind.maxHealth;
        this.health = health;
        sprites = new SpriteSet("android/assets/sprites/" + color.id() + "/germs/" + GermKind.BASIC + "/idle_", 8, 0);
//        sprites = new SpriteSet("android/assets/sprites/" + color.id() + "/germs/" + kind + "/idle_", 8, 0);
    }

    Germ(Coordinates coordinates, Color color, GermKind kind) {
        this(coordinates, color, kind, kind.maxHealth);
    }

    Germ(Color color, GermKind kind, int health) {
        this(new Coordinates(), color, kind, health);
    }

    Germ(Color color, GermKind kind) {
        this(color, kind, kind.maxHealth);
    }

    static Germ ofSymbol(char symbol, Set<Color> colors) {
        return GermSupplier.getKindOfSymbol(symbol).apply(Utils.getRandomFrom(colors));
    }

    @Override
    public Sprite getSprite() {
        return sprites.getCurrentSprite();
    }

    @Override
    public boolean isGerm() {
        return true;
    }

    @Override
    public boolean isCapsule() {
        return false;
    }

    public void takeHit() {
        if (health > 0) health--;
    }
}


class BasicGerm extends Germ {
    public BasicGerm(Coordinates coordinates, Color color) {
        super(coordinates, color, GermKind.BASIC);
    }

    public BasicGerm(Color color) {
        super(color, GermKind.BASIC);
    }
}

class WallGerm extends Germ {
    public WallGerm(Color color) {
        super(color, GermKind.WALL);
    }

    public WallGerm(Color color, int health) {
        super(color, GermKind.WALL, health);
    }
}

class VirusGerm extends Germ {
    public VirusGerm(Color color) {
        super(color, GermKind.VIRUS);
    }
}

class ThornGerm extends Germ {
    public ThornGerm(Color color) {
        super(color, GermKind.THORN);
    }
}
