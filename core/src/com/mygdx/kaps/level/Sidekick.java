package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Sidekick {
    SEAN(Color.COLOR_1, 20),
    ZYRAME(Color.COLOR_2, 18),
    RED(Color.COLOR_3, 25),
    MIMAPS(Color.COLOR_4, 15),
    PAINT(Color.COLOR_5, 10),
    XERETH(Color.COLOR_6, 25),
    BOMBER(Color.COLOR_7, -13),
    JIM(Color.COLOR_10, 18),
    COLOR(Color.COLOR_11, -4),
    SNIPER(Color.COLOR_12, 20),
    ;

    private final AnimatedSprite flippedAnim;
    private final AnimatedSprite anim;
    private final Color color;
    private final Gauge cooldown;
    private final Gauge mana;

    Sidekick(Color color, int mana) {
        var animPath = "android/assets/sprites/sidekicks/" + this + "_";
        anim = new AnimatedSprite(animPath, 4, 0.2f);
        flippedAnim = new AnimatedSprite(animPath, 4, 0.2f, true, true);
        this.cooldown = mana > 0 ? new Gauge(mana) : null;
        this.mana = mana < 0 ? Gauge.full(-mana) : null;
        this.color = color;
    }

    @Override
    public String toString() {
        var str = super.toString();
        return str.charAt(0) + str.substring(1).toLowerCase();
    }

    public Color getColor() {
        return color;
    }

    public Sprite getSprite() {
        return anim.getCurrentSprite();
    }

    public Sprite getFlippedSprite() {
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
