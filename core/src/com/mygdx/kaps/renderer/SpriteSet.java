package com.mygdx.kaps.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.time.Timer;

import java.nio.file.Path;
import java.util.Random;
import java.util.stream.IntStream;


public class SpriteSet implements Animated, RenderableDynamic {
    private final Timer updateTimer;
    private final Sprite[] sprites;
    private final boolean looping;
    private final int animationLength;
    private final Path path;
    private int currentFrame;

    public SpriteSet(String path, int frames, double speed) {
        this(path, new Random().nextInt(frames), frames, speed, true);
    }

    public SpriteSet(String path, int startingFrame, int frames, double speed, boolean looping) {
        this.looping = looping;
        updateTimer = new Timer(speed);
        sprites = new Sprite[frames];
        currentFrame = startingFrame;
        animationLength = frames;
        this.path = Path.of(path);

        updateSprites();
    }

    public static SpriteSet oneShot(String path, int frames, double speed) {
        return new SpriteSet(path, 0, frames, speed, false);
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public Sprite getCurrentSprite() {
        return sprites[currentFrame];
    }

    private void updateSprites() {
        IntStream.range(0, 8).forEach(n -> {
            String path = this.path.toString() + (animationLength >= 9 && n < 10 ? "0" : "") + n + ".png";
            var sprite = new Sprite(new Texture(path));
            sprite.flip(false, true);
            sprites[n] = sprite;
        });
    }

    @Override
    public void update() {
        if (isAtLastFrame() && !looping) return;
        if (updateTimer.resetIfExceeds()) currentFrame = (currentFrame + 1) % animationLength;
    }

    /**
     * Checks if the sprite can jump to the next frame, and updates the current frame if so.
     *
     * @return 1 if the sprite can be updated,
     * 0 if the sprite can't be updated yet,
     * -1 if it can't be updated anymore (non-looping sprite)
     */
    public int updateIfPossible() {
        if (updateTimer.resetIfExceeds()) {
            if (isAtLastFrame() && !looping) return -1;
            currentFrame = (currentFrame + 1) % animationLength;
            return 1;
        }
        return 0;
    }

    @Override
    public void render() {
//        spra.render(getCurrentSprite(false));
    }

    @Override
    public void render(float x, float y, float width, float height) {
//        spra.render(getCurrentSprite(true), x, y, width, height);
    }

    public void render(float x, float y, float width, float height, boolean right) {
//        spra.render(getCurrentSprite(right), x, y, width, height);
    }

    private boolean isAtLastFrame() {
        return currentFrame == animationLength - 1;
    }

    public boolean isOver() {
        return isAtLastFrame() && updateTimer.isExceeded();
    }

    public boolean hasExceeded(int frame) {
        return this.currentFrame >= frame;
    }
}
