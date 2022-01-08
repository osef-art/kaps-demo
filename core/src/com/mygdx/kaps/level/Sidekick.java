package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

enum Sidekick {
    SEAN(Color.COLOR_1),
    ZYRAME(Color.COLOR_2),
    R3D(Color.COLOR_3, "Red"),
    MIMAPS(Color.COLOR_4),
    PAINTER(Color.COLOR_5, "Paint"),
    XERETH(Color.COLOR_6),
    BOMBER(Color.COLOR_7),
    JIM(Color.COLOR_10),
    UNI(Color.COLOR_11, "Color"),
    SNIPER(Color.COLOR_12),
    ;

    private final AnimatedSprite flippedAnim;
    private final AnimatedSprite anim;
    private final Color color;

    Sidekick(Color color, String name) {
        var animPath = "android/assets/sprites/sidekicks/" + name + "_";
        anim = new AnimatedSprite(animPath, 4, 0.2f);
        flippedAnim = new AnimatedSprite(animPath, 4, 0.2f, true, true);
        this.color = color;
    }

    Sidekick(Color color) {
        var animPath = "android/assets/sprites/sidekicks/" + this + "_";
        anim = new AnimatedSprite(animPath, 4, 0.2f);
        flippedAnim = new AnimatedSprite(animPath, 4, 0.2f, true, true);
        this.color = color;
    }

    @Override
    public String toString() {
        var str = super.toString();
        return str.charAt(0) + str.substring(1).toLowerCase();
    }

    Color getColor() {
        return color;
    }

    Sprite getSprite() {
        return anim.getCurrentSprite();
    }

    Sprite getFlippedSprite() {
        return flippedAnim.getCurrentSprite();
    }

    void updateSprite() {
        anim.updateExistenceTime();
        flippedAnim.updateExistenceTime();
    }

    static Set<Sidekick> randomSet(int n) {
        var sidekicks = new HashSet<Sidekick>();
        while (sidekicks.size() < n)
            sidekicks.add(Utils.getRandomFrom(Arrays.stream(Sidekick.values())));
        return sidekicks;
    }
}
