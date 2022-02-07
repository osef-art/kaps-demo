package com.mygdx.kaps.level.gridobject;

import com.mygdx.kaps.Utils;

import java.util.Arrays;

public enum Color {
    COLOR_1(new java.awt.Color(110, 80, 235)),
    COLOR_2(new java.awt.Color(90, 190, 235)),
    COLOR_3(new java.awt.Color(220, 60, 40)),
    COLOR_4(new java.awt.Color(180, 235, 60)),
    COLOR_5(new java.awt.Color(50, 235, 215)),
    COLOR_6(new java.awt.Color(215, 50, 100)),
    COLOR_7(new java.awt.Color(220, 235, 160)),
    COLOR_8(new java.awt.Color(40, 50, 60), true),
    COLOR_9(new java.awt.Color(180, 200, 220), true),
    COLOR_10(new java.awt.Color(100, 110, 170)),
    COLOR_11(new java.awt.Color(50, 180, 180)),
    COLOR_12(new java.awt.Color(235, 150, 140)),
    COLOR_13(new java.awt.Color(50, 100, 225)),
//    COLOR_14(new java.awt.Color(175, 225, 235))
    ;

    private final com.badlogic.gdx.graphics.Color gdxColor;
    private final boolean blank;

    Color(java.awt.Color color) {
        this(color, false);
    }

    Color(java.awt.Color color, boolean blank) {
        this.blank = blank;
        gdxColor = new com.badlogic.gdx.graphics.Color(
          (float) (color.getRed() / 255.),
          (float) (color.getGreen() / 255.),
          (float) (color.getBlue() / 255.),
          (float) (color.getAlpha() / 255.)
        );
    }

    @Override
    public String toString() {
        return Integer.toString(ordinal() + 1);
    }

    public static Color random() {
        return Utils.getRandomFrom(values());
    }

    public static Color randomBlank() {
        return Utils.getRandomFrom(Arrays.stream(values()).filter(c -> c.blank));
    }

    public com.badlogic.gdx.graphics.Color value() {
        return gdxColor;
    }

    public com.badlogic.gdx.graphics.Color value(float alpha) {
        return new com.badlogic.gdx.graphics.Color(
          gdxColor.r, gdxColor.g, gdxColor.b, alpha
        );
    }
}
