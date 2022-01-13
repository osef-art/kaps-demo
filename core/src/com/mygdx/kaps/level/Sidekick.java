package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.renderer.AnimatedSprite;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


interface ISidekick {
    double gaugeRatio();

    void increaseMana();

    void decreaseCooldown();

    void resetGauge();

    boolean isReady();

    void ifActiveElse(Consumer<ManaSidekick> activeAction, Consumer<CooldownSidekick> passiveAction);
}

public abstract class Sidekick implements ISidekick {
    enum AttackType {
        SLICE, FIRE, FIREARM, MELEE, MAGIC, BRUSH,
    }

    enum SidekickId {
        SEAN(Color.COLOR_1, AttackType.MELEE, SidekickPower.hit1RandomObjectAndAdjacents(), 20, 2),
        ZYRAME(Color.COLOR_2, AttackType.SLICE, SidekickPower.hit2RandomGerms(), 18, 2),
        R3D(Color.COLOR_3, AttackType.SLICE, SidekickPower.hitRandomColumn(), 25, 2, "Red"),
        MIMAPS(Color.COLOR_4, AttackType.FIRE, SidekickPower.hit3RandomObjects(), 15, 2),
        PAINTER(Color.COLOR_5, AttackType.BRUSH, SidekickPower.paint5RandomObjects(), 10, 1, "Paint"),
        XERETH(Color.COLOR_6, AttackType.SLICE, SidekickPower.hitRandomDiagonals(), 25, 1),
        BOMBER(Color.COLOR_7, AttackType.FIREARM, SidekickPower.doNothing(), 13, true),
        JIM(Color.COLOR_10, AttackType.SLICE, SidekickPower.hitRandomLine(), 18, 1),
        UNI(Color.COLOR_11, AttackType.BRUSH, SidekickPower.doNothing(), 4, true, "Color"),
        SNIPER(Color.COLOR_12, AttackType.FIREARM, SidekickPower.hit1RandomGerm(), 20, 3),
        ;

        private final Consumer<Grid> power;
        private final AttackType type;
        private final boolean passive;
        private final String animPath;
        private final Color color;
        private final int damage;
        private final int mana;

        SidekickId(Color color, AttackType type, BiConsumer<SidekickId, Grid> power, int mana, boolean passive,
                   int damage, String... names) {
            var name = names.length > 0 ? names[0] : toString();
            animPath = "android/assets/sprites/sidekicks/" + name + "_";
            this.power = grid -> power.accept(this, grid);
            this.passive = passive;
            this.damage = damage;
            this.color = color;
            this.mana = mana;
            this.type = type;
        }

        SidekickId(Color color, AttackType type, BiConsumer<SidekickId, Grid> power, int mana, boolean passive,
                   String... names) {
            this(color, type, power, mana, passive, 0, names);
        }

        SidekickId(Color color, AttackType type, BiConsumer<SidekickId, Grid> power, int mana, int damage,
                   String... names) {
            this(color, type, power, mana, false, damage, names);
        }

        public String toString() {
            var str = super.toString();
            return str.charAt(0) + str.substring(1).toLowerCase();
        }

        public Color color() {
            return color;
        }

        int gaugeMax() {
            return mana;
        }

        int damage() {
            return damage;
        }
    }

    private final AnimatedSprite flippedAnim;
    private final AnimatedSprite anim;
    private final SidekickId id;

    Sidekick(SidekickId id) {
        flippedAnim = new AnimatedSprite(id.animPath, 4, 0.2f, true, true);
        anim = new AnimatedSprite(id.animPath, 4, 0.2f);
        this.id = id;
    }

    static Set<Sidekick> randomSet(int n) {
        return Utils.getRandomSetOf(Arrays.stream(SidekickId.values()).map(Sidekick::ofId), n);
    }

    static Sidekick ofId(SidekickId id) {
        return id.passive ? new CooldownSidekick(id) : new ManaSidekick(id);
    }

    public Color color() {
        return id.color;
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

    void trigger(Level level) {
        id.power.accept(level.getGrid());
    }

    void triggerIfReady(Level level) {
        if (isReady()) {
            trigger(level);
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

    int currentMana() {
        return mana.getValue();
    }

    int maxMana() {
        return mana.getMax();
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

    int turnsLeft() {
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

