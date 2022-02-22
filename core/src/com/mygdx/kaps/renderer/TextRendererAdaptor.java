package com.mygdx.kaps.renderer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;

public class TextRendererAdaptor extends ApplicationAdapter {
    private final SpriteRendererAdapter spr;
    private final BitmapFont shade;
    private final BitmapFont font;
    private final float fontSize;
    private final float offset;

    public TextRendererAdaptor(SpriteRendererAdapter spriteRenderer, int size, Color color, Color shade) {
        spr = spriteRenderer;
        fontSize = size;
        offset = fontSize / 12;

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.color = color;
        parameter.flip = true;
        parameter.size = size;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
          Gdx.files.internal("android/assets/fonts/Gotham.ttf")
        );
        font = generator.generateFont(parameter);
        this.shade = generator.generateFont(parameter);
        this.shade.setColor(shade);
        generator.dispose();
    }

    public TextRendererAdaptor(SpriteRendererAdapter spriteRenderer, int size, Color color) {
        this(spriteRenderer, size, color, new Color(0, 0, 0, .25f));
    }

    public void drawText(String txt, float x, float y) {
        spr.renderText(txt, font, x, y);
    }

    public void drawText(String txt, float x, float y, float width, float height) {
        spr.renderText(txt, font, x, y + fontSize / 4, width, height - fontSize * 2);
    }

    public void drawText(String txt, Rectangle zone) {
        drawText(txt, zone.x, zone.y, zone.width, zone.height);
    }

    public void drawTextWithShadow(String txt, float x, float y) {
        spr.renderText(txt, shade, x + offset, y + offset);
        drawText(txt, x, y);
    }

    public void drawTextWithShadow(String txt, Rectangle rect) {
        drawTextWithShadow(txt, rect.x, rect.y, rect.width, rect.height);
    }

    public void drawTextWithShadow(String txt, float x, float y, float width, float height) {
        spr.renderText(txt, shade, x, y + fontSize / 4, width, height - fontSize * 2);
        drawText(txt, x, y, width, height);
    }

    @Override
    public void dispose() {
        shade.dispose();
        font.dispose();
    }
}
