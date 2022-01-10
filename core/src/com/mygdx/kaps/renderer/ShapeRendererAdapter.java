package com.mygdx.kaps.renderer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.function.Consumer;

import static com.mygdx.kaps.MainScreen.camera;

public class ShapeRendererAdapter implements RendererAdapter {
    private final ShapeRenderer rd = new ShapeRenderer();

    private void draw(Consumer<ShapeRenderer> action, Color color) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        rd.setProjectionMatrix(camera.combined);
        rd.begin(ShapeRenderer.ShapeType.Filled);
        rd.setColor(color.r, color.g, color.b, color.a);
        action.accept(rd);
        rd.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void drawCircle(float x, float y, float radius, Color color) {
        draw(rd -> rd.circle(x, y, radius), color);
    }

    public void drawCircle(Rectangle zone, Color color) {
        drawCircle(zone.x + zone.width / 2, zone.y + zone.height / 2, Math.min(zone.width, zone.height) / 2, color);
    }

    public void drawArc(float x, float y, float radius, float start, float degrees, Color color) {
        draw(rd -> rd.arc(x, y, radius, start, degrees), color);
    }

    public void drawRect(float x, float y, float w, float h, Color color) {
        draw(rd -> rd.rect(x, y, w, h), color);
    }

    public void drawRect(Rectangle zone, Color color) {
        drawRect(zone.x, zone.y, zone.width, zone.height, color);
    }

    public void drawLine(Vector2 origin, Vector2 vector, Color color) {
        draw(rd -> rd.line(origin, vector), color);
    }

    public void renderGauge(Rectangle rectangle, double ratio, Color back, Color front, boolean reversed) {
        drawRect(rectangle, back);
        drawRect(
          rectangle.x + (reversed ? rectangle.width * (1 - (float) ratio) : 0),
          rectangle.y,
          (float) ratio * rectangle.width,
          rectangle.height,
          front
        );
    }

    public void renderGauge(Rectangle rectangle, double ratio, Color back, Color front) {
        renderGauge(rectangle, ratio, back, front, false);
    }

    @Override
    public void dispose() {
        rd.dispose();
    }
}
