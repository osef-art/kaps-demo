package com.mygdx.kaps.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Objects;
import java.util.stream.IntStream;

public class AnimatedSprite {
    private final Animation<Sprite> anim;
    private float existenceTime = 0;
    private final boolean looping;

    public AnimatedSprite(String path, int nbFrames, float animSpeed) {
        this(path, nbFrames, animSpeed, true, false);
    }

    public AnimatedSprite(String path, int nbFrames, float animSpeed, boolean looping, boolean flip) {
        var frames = new Sprite[nbFrames];
        IntStream.range(0, nbFrames).forEach(n -> {
            var sprite = new Sprite(new Texture(
              Objects.requireNonNull(path) + (nbFrames >= 9 && n < 10 ? "0" : "") + n + ".png"
            ));
            sprite.flip(flip, true);
            frames[n] = sprite;
        });
        anim = new Animation<>(animSpeed, frames);
        this.looping = looping;
    }

    public Sprite getCurrentSprite() {
        return anim.getKeyFrame(existenceTime, looping);
    }

    public void updateExistenceTime() {
        existenceTime += Gdx.graphics.getDeltaTime();
    }

    public void reset() {
        existenceTime = 0;
    }

    public boolean isFinished() {
        return anim.isAnimationFinished(existenceTime);
    }
}
