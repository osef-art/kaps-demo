package com.mygdx.kaps.level;

import com.mygdx.kaps.level.gridobject.Color;

import java.util.Arrays;
import java.util.function.BiFunction;

public enum SidekickId {
    SEAN(Color.COLOR_1, AttackType.MELEE, SidekickAttack::hitRandomObjectAndAdjacents, 20, 2),
    ZYRAME(Color.COLOR_2, AttackType.SLICE, SidekickAttack::hit2RandomGerms, 18, 2),
    R3D(Color.COLOR_3, AttackType.SLICE, SidekickAttack::hitRandomColumn, 25, 2, "Red"),
    MIMAPS(Color.COLOR_4, AttackType.FIRE, SidekickAttack::hit3RandomObjects, 15, 2),
    PAINTER(Color.COLOR_5, AttackType.BRUSH, SidekickAttack::paint5RandomObjects, 10, 1, "Paint"),
    XERETH(Color.COLOR_6, AttackType.SLICE, SidekickAttack::hitRandomDiagonals, 25, 1),
    BOMBER(Color.COLOR_7, AttackType.FIREARM, (sdk, lvl) -> SidekickAttack.injectExplosiveCapsule(lvl), 13, true),
    JIM(Color.COLOR_10, AttackType.SLICE, SidekickAttack::hitRandomLine, 18, 1),
    UNI(Color.COLOR_11, AttackType.BRUSH, (sdk, lvl) -> SidekickAttack.injectMonoColorCapsule(lvl), 4, true, "Color"),
    SNIPER(Color.COLOR_12, AttackType.FIREARM, SidekickAttack::hitRandomGerm, 20, 3),
    ;

    final BiFunction<Sidekick, Level, SidekickAttack> attack;
    final AttackType type;
    final boolean passive;
    final String animPath;
    final Color color;
    final int damage;
    final int mana;

    SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> attack,
               int mana, boolean passive, int damage, String... names) {
        var name = names.length > 0 ? names[0] : toString();
        animPath = "android/assets/sprites/sidekicks/" + name + "_";
        this.passive = passive;
        this.attack = attack;
        this.damage = damage;
        this.color = color;
        this.mana = mana;
        this.type = type;
    }

    SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> atk, int mana, boolean passive, String... names) {
        this(color, type, atk, mana, passive, 0, names);
    }

    SidekickId(Color color, AttackType type, BiFunction<Sidekick, Level, SidekickAttack> atk, int mana, int damage, String... names) {
        this(color, type, atk, mana, false, damage, names);
    }

    @Override
    public String toString() {
        var str = super.toString();
        return str.charAt(0) + str.substring(1).toLowerCase();
    }

    public Color color() {
        return color;
    }

    public String getAnimPath() {
        return animPath;
    }

    int gaugeMax() {
        return mana;
    }

    static SidekickId ofName(String name) {
        return Arrays.stream(values())
          .filter(s -> s.toString().equalsIgnoreCase(name))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Can't resolve sidekick of name " + name));
    }
}
