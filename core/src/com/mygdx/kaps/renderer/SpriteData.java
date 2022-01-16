package com.mygdx.kaps.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.gridobject.Color;
import com.mygdx.kaps.level.gridobject.Germ;
import com.mygdx.kaps.level.gridobject.Orientation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class SpriteData {
    private static final float poppingSpeed = 0.075f;
    private static final String SPRITES_PATH = "android/assets/sprites";
    private static final HashMap<Color, HashMap<Orientation, Sprite>> capsules = new HashMap<>();
    private static final HashMap<Color, HashMap<Germ.GermKind, AnimatedSprite>> germs = new HashMap<>();
    private static final HashMap<Color, List<AnimatedSprite>> wallGerms = new HashMap<>();

    public SpriteData() {
        Arrays.stream(Color.values()).forEach(color -> {
            wallGerms.put(color, new ArrayList<>());
            capsules.put(color, new HashMap<>());
            germs.put(color, new HashMap<>());

            Arrays.stream(Orientation.values()).forEach(o -> {
                var sprite = new Sprite(
                  new Texture(SPRITES_PATH + "/caps/color" + color.id() + "/" + o + ".png")
                );
                sprite.flip(false, true);
                capsules.get(color).put(o, sprite);
            });

            Arrays.stream(Germ.GermKind.values()).forEach(k -> {
                if (k == Germ.GermKind.WALL) {
                    IntStream.range(0, 4).forEach(
                      n -> wallGerms.get(color).add(new AnimatedSprite(
                        SPRITES_PATH + "/germs/" + k + "/level" + (n + 1) + "/color" + color.id() + "/idle_",
                        n > 1 ? 4 : 8,
                        n > 1 ? .2f : .15f
                      ))
                    );
                    return;
                }
                germs.get(color).put(k, new AnimatedSprite(
                  SPRITES_PATH + "/germs/" + k + "/color" + color.id() + "/idle_", 8, k.getAnimationSpeed()
                ));
            });
        });
    }

    public void updateSprites() {
        germs.values().forEach(m -> m.values().forEach(AnimatedSprite::updateExistenceTime));
        wallGerms.values().forEach(lst -> lst.forEach(AnimatedSprite::updateExistenceTime));
    }


    private static AnimatedSprite poppingAnimation(String path, Color color) {
        return new AnimatedSprite(SPRITES_PATH + path + "/color" + color.id() + "/pop_", 8, poppingSpeed);
    }

    public static AnimatedSprite poppingAnimation(Color color) {
        return poppingAnimation("/caps", color);
    }

    public static AnimatedSprite poppingGermAnimation(Germ.GermKind kind, Color color) {
        return poppingAnimation("/germs/" + kind, color);
    }

    public static AnimatedSprite poppingWallAnimation(int health, Color color) {
        return poppingAnimation("/germs/" + Germ.GermKind.WALL + "/level" + (health + 1), color);
    }

    public Sprite getCapsule(Orientation orientation, Color color) {
        return capsules.get(color).get(orientation);
    }

    public AnimatedSprite getGerm(Germ.GermKind kind, Color color) {
        return germs.get(color).get(kind);
    }

    public AnimatedSprite getWallGerm(int health, Color color) {
        return wallGerms.get(color).get(health - 1);
    }
}
