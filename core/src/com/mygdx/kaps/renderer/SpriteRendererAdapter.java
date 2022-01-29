package com.mygdx.kaps.renderer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.function.Consumer;

public class SpriteRendererAdapter extends ApplicationAdapter {
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera;

    public SpriteRendererAdapter(OrthographicCamera cam) {
        camera = cam;
    }

    private void draw(Consumer<SpriteBatch> action) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        action.accept(batch);
        batch.end();
    }

    public void render(Sprite sprite, float x, float y, float width, float height) {
        draw(b -> b.draw(sprite, x, y, width, height));
    }

    public void render(Sprite sprite, float x, float y, float width, float height, float alpha) {
        draw(b -> {
            b.setColor(1, 1, 1, alpha);
            b.draw(sprite, x, y, width, height);
            b.setColor(1, 1, 1, 1);
        });
    }

    public void render(Sprite sprite, Rectangle rectangle) {
        render(sprite, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public void render(Sprite sprite, Rectangle rectangle, float alpha) {
        render(sprite, rectangle.x, rectangle.y, rectangle.width, rectangle.height, alpha);
    }

    void renderText(String text, BitmapFont font, float x, float y) {
        draw(b -> font.draw(b, text, x, y));
    }

    void renderText(String text, BitmapFont font, float x, float y, float width, float height) {
        final GlyphLayout layout = new GlyphLayout(font, text);
        final float fontX = x + (width - layout.width) / 2;
        final float fontY = y + (height + layout.height) / 2;

        draw(b -> font.draw(b, layout, fontX, fontY));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
