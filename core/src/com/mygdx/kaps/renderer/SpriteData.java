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
    private static final HashMap<Color, AnimatedSprite> capsulePop = new HashMap<>();
    private static final HashMap<Color, HashMap<Germ.GermKind, AnimatedSprite>> germs = new HashMap<>();
    private static final HashMap<Color, HashMap<Germ.GermKind, AnimatedSprite>> germsPop = new HashMap<>();
    private static final HashMap<Color, List<AnimatedSprite>> wallGerms = new HashMap<>();
    private static final HashMap<Color, List<AnimatedSprite>> wallGermsPop = new HashMap<>();

    public SpriteData() {
        Arrays.stream(Color.values()).forEach(color -> {
            capsules.put(color, new HashMap<>());
            germsPop.put(color, new HashMap<>());
            germs.put(color, new HashMap<>());
            wallGermsPop.put(color, new ArrayList<>());
            wallGerms.put(color, new ArrayList<>());
            capsulePop.put(color, new AnimatedSprite(SPRITES_PATH + "/caps/color" + color.id() + "/pop_", 8, poppingSpeed));

            Arrays.stream(Orientation.values()).forEach(o -> {
                var sprite = new Sprite(
                  new Texture(SPRITES_PATH + "/caps/color" + color.id() + "/" + o + ".png")
                );
                sprite.flip(false, true);
                capsules.get(color).put(o, sprite);
            });

            Arrays.stream(Germ.GermKind.values()).forEach(k -> {
                if (k == Germ.GermKind.WALL) {
                    IntStream.range(0, 4).forEach(n -> {
                        wallGerms.get(color).add(new AnimatedSprite(
                          SPRITES_PATH + "/germs/" + k + "/level" + (n + 1) + "/color" + color.id() + "/idle_",
                          n > 1 ? 4 : 8,
                          n > 1 ? .2f : .15f
                        ));
                        wallGermsPop.get(color).add(new AnimatedSprite(
                          SPRITES_PATH + "/germs/" + k + "/level" + (n + 1) + "/color" + color.id() + "/pop_", 8,
                          poppingSpeed
                        ));
                    });
                    return;
                }
                germs.get(color).put(k, new AnimatedSprite(
                  SPRITES_PATH + "/germs/" + k + "/color" + color.id() + "/idle_", 8, k.getAnimationSpeed()
                ));
                germsPop.get(color).put(k, new AnimatedSprite(
                  SPRITES_PATH + "/germs/" + k + "/color" + color.id() + "/pop_", 8, poppingSpeed
                ));
            });
        });
    }

    public void updateSprites() {
        germs.values().forEach(m -> m.values().forEach(AnimatedSprite::updateExistenceTime));
        wallGerms.values().forEach(lst -> lst.forEach(AnimatedSprite::updateExistenceTime));
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
