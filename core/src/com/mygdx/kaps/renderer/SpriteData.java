package com.mygdx.kaps.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.AttackType;
import com.mygdx.kaps.level.SidekickId;
import com.mygdx.kaps.level.gridobject.*;

import java.util.*;
import java.util.stream.IntStream;

@SuppressWarnings("DefaultLocale")
public class SpriteData {
    private static final float poppingSpeed = .065f;
    private static final String SPRITES_PATH = "android/assets/sprites";
    private static final Map<Color, Map<CapsulePart.BonusType, Map<Orientation, Sprite>>> capsules = new HashMap<>();
    private static final Map<Color, Map<Germ.GermKind, AnimatedSprite>> germs = new HashMap<>();
    private static final Map<Color, List<AnimatedSprite>> wallGerms = new HashMap<>();
    private static final Map<SidekickId, Map<Boolean, AnimatedSprite>> sidekicks = new HashMap<>();

    public SpriteData() {
        Arrays.stream(Color.values()).forEach(color -> {
            wallGerms.put(color, new ArrayList<>());
            capsules.put(color, new HashMap<>());
            germs.put(color, new HashMap<>());

            Arrays.stream(CapsulePart.BonusType.values()).forEach(type -> {
                capsules.get(color).put(type, new HashMap<>());

                Arrays.stream(Orientation.values()).forEach(o -> {
                    var sprite = new Sprite(new Texture(String.format(
                      "%s/caps/%s/color%s/%s.png", SPRITES_PATH, type == CapsulePart.BonusType.EXPLOSIVE ? "bomb" : "", color, o)
                    ));
                    sprite.flip(false, true);
                    capsules.get(color).get(type).put(o, sprite);
                });
            });

            Arrays.stream(Germ.GermKind.values()).forEach(kind -> {
                if (kind == Germ.GermKind.WALL)
                    IntStream.range(0, 4).forEach(n -> wallGerms.get(color).add(new AnimatedSprite(
                      String.format("%s/germs/%s/level%d/color%s/idle_", SPRITES_PATH, kind, n + 1, color),
                      n > 1 ? 4 : 8,
                      n > 1 ? .2f : .15f
                    )));
                else
                    germs.get(color).put(kind, new AnimatedSprite(String.format(
                      "%s/germs/%s/color%s/idle_", SPRITES_PATH, kind, color), 8, kind.getAnimationSpeed()
                    ));
            });
        });

        Arrays.stream(SidekickId.values()).forEach(id -> {
            sidekicks.put(id, new HashMap<>());

            Arrays.asList(true, false).forEach(
              left -> sidekicks.get(id).put(left, new AnimatedSprite(String.format("%s/%s", SPRITES_PATH, id.getAnimPath()), 4, .2f, true, left))
            );
        });
    }

    public void updateSprites() {
        germs.values().forEach(m -> m.values().forEach(AnimatedSprite::updateExistenceTime));
        wallGerms.values().forEach(lst -> lst.forEach(AnimatedSprite::updateExistenceTime));
        sidekicks.values().forEach(m -> m.values().forEach(AnimatedSprite::updateExistenceTime));
    }

    private static float randomAnimSpeed() {
        return poppingSpeed + new Random().nextFloat() * .1f;
    }

    private static AnimatedSprite poppingAnimation(String path, Color color) {
        return new AnimatedSprite(String.format("%s/%s/color%s/pop_", SPRITES_PATH, path, color), 8, poppingSpeed);
    }

    public static AnimatedSprite poppingAnimation(Color color) {
        return poppingAnimation("caps", color);
    }

    public static AnimatedSprite poppingGermAnimation(Germ.GermKind kind, Color color) {
        return poppingAnimation("germs/" + kind, color);
    }

    public static AnimatedSprite poppingWallAnimation(int health, Color color) {
        return poppingAnimation(String.format("germs/%s/level%d", Germ.GermKind.WALL, health + 1), color);
    }

    public static AnimatedSprite attackEffect(AttackType type) {
        return new AnimatedSprite(String.format("%s/fx/%s_", SPRITES_PATH, type), 8, randomAnimSpeed());
    }

    public static AnimatedSprite attackEffect(CooldownGerm germ) {
        return new AnimatedSprite(String.format("%s/germs/%s/color%s/atk_", SPRITES_PATH, germ.kind(), germ.color()), 8, randomAnimSpeed());
    }

    public Sprite getCapsule(Orientation orientation, Color color, CapsulePart.BonusType type) {
        return capsules.get(color).get(type).get(orientation);
    }

    public AnimatedSprite getGerm(Germ.GermKind kind, Color color) {
        return germs.get(color).get(kind);
    }

    public AnimatedSprite getWallGerm(int health, Color color) {
        return wallGerms.get(color).get(health - 1);
    }

    public AnimatedSprite getSidekick(SidekickId id, boolean left) {
        return sidekicks.get(id).get(left);
    }
}
