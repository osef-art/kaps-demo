package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


interface ISidekick {
    double gaugeRatio();

    void increaseMana();

    void decreaseCooldown();

    void resetGauge();

    boolean isReady();
}

public abstract class Sidekick implements ISidekick {
    enum SidekickData {
        SEAN(Color.COLOR_1, 20),
        ZYRAME(Color.COLOR_2, 18),
        R3D(Color.COLOR_3, 25, "Red"),
        MIMAPS(Color.COLOR_4, 15),
        PAINTER(Color.COLOR_5, 10, "Paint"),
        XERETH(Color.COLOR_6, 25),
        BOMBER(Color.COLOR_7, 13),
        JIM(Color.COLOR_10, 18),
        UNI(Color.COLOR_11, 4, "Color"),
        SNIPER(Color.COLOR_12, 20),
        ;

        private final String animPath;
        private final Color color;
        private final int mana;

        SidekickData(Color color, int mana, String... names) {
            var name = names.length > 0 ? names[0] : toString();
            animPath = "android/assets/sprites/sidekicks/" + name + "_";
            this.color = color;
            this.mana = mana;
        }

        @Override
        public String toString() {
            var str = super.toString();
            return str.charAt(0) + str.substring(1).toLowerCase();
        }

        public int gaugeMax() {
            return mana;
        }
    }
    enum SidekickSupplier {
        SEAN(new ManaSidekick(SidekickData.SEAN)),
        ZYRAME(new ManaSidekick(SidekickData.ZYRAME)),
        R3D(new ManaSidekick(SidekickData.R3D)),
        MIMAPS(new ManaSidekick(SidekickData.MIMAPS)),
        PAINTER(new ManaSidekick(SidekickData.PAINTER)),
        XERETH(new ManaSidekick(SidekickData.XERETH)),
        BOMBER(new CooldownSidekick(SidekickData.BOMBER)),
        JIM(new ManaSidekick(SidekickData.JIM)),
        UNI(new CooldownSidekick(SidekickData.UNI)),
        SNIPER(new ManaSidekick(SidekickData.SNIPER)),
        ;

        private final Sidekick sidekick;

        SidekickSupplier(Sidekick sdk) {
            sidekick = sdk;
        }
    }

    private final AnimatedSprite flippedAnim;
    private final AnimatedSprite anim;
    private final Color color;

    Sidekick(SidekickData data) {
        color = data.color;
        anim = new AnimatedSprite(data.animPath, 4, 0.2f);
        flippedAnim = new AnimatedSprite(data.animPath, 4, 0.2f, true, true);
    }

    static Set<Sidekick> randomSet(int n) {
        var sidekicks = new HashSet<SidekickSupplier>();
        while (sidekicks.size() < n)
            sidekicks.add(Utils.getRandomFrom(Arrays.stream(SidekickSupplier.values())));
        return sidekicks.stream().map(s -> s.sidekick).collect(Collectors.toSet());
    }

    public Color color() {
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

    void trigger() {

    }

    void triggerIfReady() {
        if (isReady()) {
            trigger();
          resetGauge();
        }
    }
}

class ManaSidekick extends Sidekick{
    private final Gauge mana;

    ManaSidekick(Sidekick.SidekickData data) {
        super(data);
        this.mana = new Gauge(data.gaugeMax());
    }

    public String toString() {
        return mana.toString();
    }

    public double gaugeRatio() {
        return mana.ratio();
    }

    public void increaseMana() {
        mana.increase();
    }

    public void decreaseCooldown() {
    }

    public void resetGauge() {
        mana.empty();
    }


    @Override
    public boolean isReady() {
        return mana.isFull();
    }
}

class CooldownSidekick extends Sidekick{
    private final Gauge cooldown;

    CooldownSidekick(Sidekick.SidekickData data) {
        super(data);
        this.cooldown = Gauge.full(data.gaugeMax());
    }

    public String toString() {
        return cooldown.toString();
    }

    public double gaugeRatio() {
        return cooldown.ratio();
    }

    public void increaseMana() {
    }

    public void decreaseCooldown() {
        cooldown.decreaseIfPossible();
    }

    public void resetGauge() {
        cooldown.fill();
    }

    @Override
    public boolean isReady() {
        return cooldown.isEmpty();
    }
}

