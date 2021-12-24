package com.mygdx.kaps.renderer;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.mygdx.kaps.MainScreen.camera;

public class SpriteRendererAdapter implements RendererAdapter {
    private final SpriteBatch batch = new SpriteBatch();

    private void draw(Runnable action) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        action.run();
        batch.end();
    }

    public void render(Sprite sprite) {
        draw(() -> sprite.draw(batch));
    }

    public void render(Sprite sprite, float x, float y, float width, float height) {
        draw(() -> batch.draw(sprite, x, y, width, height));
    }

    public void render(Sprite sprite, float x, float y, float width, float height, float alpha) {
        draw(() -> {
            batch.setColor(1,1,1, alpha);
            batch.draw(sprite, x, y, width, height);
            batch.setColor(1,1,1, 1);
        });
    }

    void renderText(String text, BitmapFont font, float x, float y) {
        draw(() -> font.draw(batch, text, x, y));
    }

    void renderText(String txt, BitmapFont font, float x, float y, float width, float height) {
        final GlyphLayout layout = new GlyphLayout(font, txt);
        final float fontX = x + (width - layout.width) / 2;
        final float fontY = y + (height + layout.height) / 2;

        draw(() -> font.draw(batch, layout, fontX, fontY));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
