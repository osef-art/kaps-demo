package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.renderer.SpriteSet;

import java.util.Arrays;
import java.util.Set;

public class Germ extends GridObject {
    enum GermKind {
        BASIC('B'), WALL('W'), THORN('T'), VIRUS('V');

        private final char symbol;

        GermKind(char sym) {
            symbol = sym;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public static GermKind getKindOfSymbol(char symbol) {
            return Arrays.stream(values())
              .filter(k -> k.symbol == symbol)
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Couldn't resolve germ with symbol: " + symbol));
        }
    }

    private final GermKind kind;
    private final SpriteSet sprites;

    private Germ(Coordinates coordinates, Color color, GermKind kind) {
        super(coordinates, color);
        this.kind = kind;
        sprites = new SpriteSet("android/assets/sprites/" + color.id() + "/germs/" + GermKind.BASIC + "/idle_", 8, 0);
//        sprites = new SpriteSet("android/assets/sprites/" + color.id() + "/germs/" + kind + "/idle_", 8, 0);
    }

    Germ(Coordinates coordinates, Color color) {
        this(coordinates, color, GermKind.BASIC);
    }

    static Germ ofSymbol(char symbol, Set<Color> colors) {
        return new Germ(new Coordinates(0, 0), Utils.getRandomFrom(colors), GermKind.getKindOfSymbol(symbol));
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
}
