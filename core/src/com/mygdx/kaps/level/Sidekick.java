package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


interface ISidekick {
    double gaugeRatio();

    void increaseMana();

    void decreaseCooldown();

    void resetGauge();

    boolean isReady();

    void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction);
}

public abstract class Sidekick implements ISidekick {
    enum SidekickId {
        SEAN(Color.COLOR_1, 20),
        ZYRAME(Color.COLOR_2, 18),
        R3D(Color.COLOR_3, 25, "Red"),
        MIMAPS(Color.COLOR_4, 15),
        PAINTER(Color.COLOR_5, 10, "Paint"),
        XERETH(Color.COLOR_6, 25),
        BOMBER(Color.COLOR_7, 13, true),
        JIM(Color.COLOR_10, 18),
        UNI(Color.COLOR_11, 4, true, "Color"),
        SNIPER(Color.COLOR_12, 20),
        ;

        private final boolean passive;
        private final String animPath;
        private final Color color;
        private final int mana;

        SidekickId(Color color, int mana, boolean passive, String... names) {
            var name = names.length > 0 ? names[0] : toString();
            animPath = "android/assets/sprites/sidekicks/" + name + "_";
            this.passive = passive;
            this.color = color;
            this.mana = mana;
        }

        SidekickId(Color color, int mana, String... names) {
            this(color, mana, false, names);
        }

        public String toString() {
            var str = super.toString();
            return str.charAt(0) + str.substring(1).toLowerCase();
        }

        public int gaugeMax() {
            return mana;
        }
    }

    private final AnimatedSprite flippedAnim;
    private final AnimatedSprite anim;
    private final Color color;

    Sidekick(SidekickId data) {
        flippedAnim = new AnimatedSprite(data.animPath, 4, 0.2f, true, true);
        anim = new AnimatedSprite(data.animPath, 4, 0.2f);
        color = data.color;
    }

    static Set<Sidekick> randomSet(int n) {
        var sdkList = Arrays.stream(SidekickId.values())
          .map(Sidekick::ofId)
          .collect(Collectors.toList());
        Collections.shuffle(sdkList);
        return new HashSet<>(sdkList.subList(0, n));
    }

    static Sidekick ofId(SidekickId id) {
        return id.passive ? new CooldownSidekick(id) : new ManaSidekick(id);
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

class ManaSidekick extends Sidekick {
    private final Gauge mana;

    ManaSidekick(SidekickId id) {
        super(id);
        this.mana = new Gauge(id.gaugeMax());
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

    public boolean isReady() {
        return mana.isFull();
    }

    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        activeAction.accept(this);
    }
}

class CooldownSidekick extends Sidekick {
    private final Gauge cooldown;

    CooldownSidekick(SidekickId id) {
        super(id);
        this.cooldown = Gauge.full(id.gaugeMax());
    }

    public String toString() {
        return cooldown.toString();
    }

    public int turnsLeft() {
        return cooldown.getValue();
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

    public boolean isReady() {
        return cooldown.isEmpty();
    }

    public void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction) {
        passiveAction.accept(this);
    }
}

