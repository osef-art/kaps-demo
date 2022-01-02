package com.mygdx.kaps.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;

public class TextRendererAdaptor implements RendererAdapter {
    private final SpriteRendererAdapter spra;
    private final BitmapFont shade;
    private final BitmapFont font;
    private final float fontSize;

    public TextRendererAdaptor(SpriteRendererAdapter spra, int size, Color color) {
        this.spra = spra;
        fontSize = size;

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.color = color;
        parameter.flip = true;
        parameter.size = size;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
          Gdx.files.internal("android/assets/fonts/Gotham.ttf")
        );
        font = generator.generateFont(parameter);
        shade = generator.generateFont(parameter);
        shade.setColor(0, 0, 0, .25f);
        generator.dispose();
    }

    public void drawText(String txt, float x, float y) {
        spra.renderText(txt, font, x, y);
    }

    public void drawText(String txt, float x, float y, float width, float height) {
        spra.renderText(txt, font, x, y, width, height - fontSize * 2f);
    }

    public void drawText(String txt, Rectangle zone) {
        drawText(txt, zone.x, zone.y, zone.width, zone.height);
    }

    public void drawTextWithShadow(String txt, float x, float y) {
        spra.renderText(txt, shade, x, y + fontSize * .2f);
        drawText(txt, x, y);
    }

    public void drawTextWithShadow(String txt, float x, float y, float width, float height) {
        spra.renderText(txt, shade, x, y + fontSize * .2f, width, height - fontSize * 2f);
        drawText(txt, x, y, width, height);
    }

    public void drawTextWithShadow(String txt, Rectangle rect) {
        drawTextWithShadow(txt, rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
