package com.mygdx.kaps.level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.sound.SoundStream;

import java.util.Arrays;
import java.util.Objects;
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
        SLICE(SoundStream.SoundStore.SLICE),
        FIRE(SoundStream.SoundStore.FIRE),
        FIREARM(SoundStream.SoundStore.SHOT),
        MELEE(SoundStream.SoundStore.SHOT),
        MAGIC(SoundStream.SoundStore.SLICE),
        BRUSH(SoundStream.SoundStore.PAINT),
        ;
        private final SoundStream.SoundStore sound;

        AttackType(SoundStream.SoundStore sound) {
            this.sound = sound;
        }
    }

    enum SidekickId {
        SEAN(Color.COLOR_1, AttackType.MELEE, SidekickAttack.hit1RandomObjectAndAdjacents(), 20, 2),
        ZYRAME(Color.COLOR_2, AttackType.SLICE, SidekickAttack.hit2RandomGerms(), 18, 2),
        R3D(Color.COLOR_3, AttackType.SLICE, SidekickAttack.hitRandomColumn(), 25, 2, "Red"),
        MIMAPS(Color.COLOR_4, AttackType.FIRE, SidekickAttack.hit3RandomObjects(), 15, 2),
        PAINTER(Color.COLOR_5, AttackType.BRUSH, SidekickAttack.paint5RandomObjects(), 10, 1, "Paint"),
        XERETH(Color.COLOR_6, AttackType.SLICE, SidekickAttack.hitRandomDiagonals(), 25, 1),
        BOMBER(Color.COLOR_7, AttackType.FIREARM, SidekickAttack.doNothing(), 13, true),
        JIM(Color.COLOR_10, AttackType.SLICE, SidekickAttack.hitRandomLine(), 18, 1),
        UNI(Color.COLOR_11, AttackType.BRUSH, SidekickAttack.doNothing(), 4, true, "Color"),
        SNIPER(Color.COLOR_12, AttackType.FIREARM, SidekickAttack.hit1RandomGerm(), 20, 3),
        ;

        private final SidekickAttack attack;
        private final AttackType type;
        private final boolean passive;
        private final String animPath;
        private final Color color;
        private final int damage;
        private final int mana;

        SidekickId(Color color, AttackType type, SidekickAttack attack, int mana, boolean passive,
                   int damage, String... names) {
            var name = names.length > 0 ? names[0] : toString();
            animPath = "android/assets/sprites/sidekicks/" + name + "_";
            this.attack = attack;
            this.passive = passive;
            this.damage = damage;
            this.color = color;
            this.mana = mana;
            this.type = type;
        }

        SidekickId(Color color, AttackType type, SidekickAttack atk, int mana, boolean passive, String... names) {
            this(color, type, atk, mana, passive, 0, names);
        }

        SidekickId(Color color, AttackType type, SidekickAttack atk, int mana, int damage, String... names) {
            this(color, type, atk, mana, false, damage, names);
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

        private static SidekickId ofName(String name) {
            return Arrays.stream(values())
              .filter(s -> s.toString().equalsIgnoreCase(name))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Can't resolve sidekick of name " + name));
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

    static Sidekick ofId(SidekickId id) {
        return id.passive ? new CooldownSidekick(id) : new ManaSidekick(id);
    }

    public static Sidekick ofName(String name) {
        return ofId(SidekickId.ofName(name));
    }

    static Sidekick random() {
        return ofId(Utils.getRandomFrom(Arrays.stream(SidekickId.values())));
    }

    static Sidekick randomMate(Set<Sidekick> mates, Sidekick sdk) {
        return Utils.getOptionalRandomFrom(mates).orElse(sdk);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sidekick)) return false;
        Sidekick sidekick = (Sidekick) o;
        return id == sidekick.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    int damage() {
        return id.damage;
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
        id.attack.perform(this, level);
    }

    void triggerIfReady(Level level) {
        if (isReady()) {
            trigger(level);
            resetGauge();
        }
    }

    SoundStream.SoundStore sound() {
        return id.type.sound;
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

class SidekickAttack {
    private final BiConsumer<Sidekick, Level> attack;

    public SidekickAttack(BiConsumer<Sidekick, Level> attack) {
        this.attack = (sdk, lvl) -> {
            attack.accept(sdk, lvl);
            lvl.getGrid().initEveryCapsuleDropping();
        };
    }

    public static SidekickAttack paint5RandomObjects() {
        return new SidekickAttack((sdk, lvl) -> {
            var mate = Sidekick.randomMate(lvl.matesOf(sdk), sdk);
            Utils.getRandomSetOf(lvl.getGrid().capsuleStack().filter(o -> o.color() != mate.color()), 5)
              .forEach(o -> lvl.getGrid().repaint(o, mate.color()));
        });
    }

    public static SidekickAttack hit3RandomObjects() {
        return new SidekickAttack(
          (sdk, lvl) -> Utils.getRandomSetOf(lvl.getGrid().stack(), 3)
            .forEach(o -> lvl.getGrid().hit(o, sdk.damage()))
        );
    }

    public static SidekickAttack hit1RandomObjectAndAdjacents() {
        return new SidekickAttack((sdk, lvl) -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(o -> {
            lvl.getGrid().hit(o, sdk.damage());
            Arrays.asList(new Coordinates(0, 1), new Coordinates(0, -1), new Coordinates(1, 0), new Coordinates(-1, 0))
              .forEach(c -> lvl.getGrid().hit(c.addedTo(o.coordinates())));
        }));
    }

    public static SidekickAttack hit2RandomGerms() {
        return new SidekickAttack(
          (sdk, lvl) -> Utils.getRandomSetOf(lvl.getGrid().germStack(), 2)
            .forEach(g -> lvl.getGrid().hit(g, sdk.damage()))
        );
    }

    public static SidekickAttack hit1RandomGerm() {
        return new SidekickAttack(
          (sdk, lvl) -> Utils.getOptionalRandomFrom(lvl.getGrid().germStack())
            .ifPresent(g -> lvl.getGrid().hit(g, sdk.damage()))
        );
    }

    public static SidekickAttack hitRandomLine() {
        return new SidekickAttack((sdk, lvl) -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(
          picked -> lvl.getGrid().stack().stream()
            .filter(o -> o.coordinates().y == picked.coordinates().y)
            .forEach(o -> lvl.getGrid().hit(o, sdk.damage()))
        ));
    }

    public static SidekickAttack hitRandomColumn() {
        return new SidekickAttack((sdk, lvl) -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(
          picked -> lvl.getGrid().stack().stream()
            .filter(o -> o.coordinates().x == picked.coordinates().x)
            .forEach(o -> lvl.getGrid().hit(o, sdk.damage()))
        ));
    }

    public static SidekickAttack hitRandomDiagonals() {
        return new SidekickAttack((sdk, lvl) -> Utils.getOptionalRandomFrom(lvl.getGrid().stack()).ifPresent(
          picked -> lvl.getGrid().stack().stream()
            .filter(o -> Math.abs(o.coordinates().x - picked.coordinates().x) ==
                           Math.abs(o.coordinates().y - picked.coordinates().y)
            )
            .forEach(o -> lvl.getGrid().hit(o, sdk.damage()))
        ));
    }

    public static SidekickAttack doNothing() {
        return new SidekickAttack((sdk, lvl) -> {
        });
    }

    public void perform(Sidekick sidekick, Level level) {
        attack.accept(sidekick, level);
    }
}
