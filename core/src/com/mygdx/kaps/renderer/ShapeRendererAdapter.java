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

    public void drawArc(Rectangle zone, float start, float degrees, Color color) {
        drawArc(zone.x + zone.width / 2, zone.y + zone.height / 2, Math.min(zone.width, zone.height) / 2, start, degrees, color);
    }

    private void drawArc(float x, float y, float radius, float start, float degrees, Color color) {
        draw(rd -> rd.arc(x, y, radius, start, degrees), color);
    }

    private void drawRoundedRect(float x, float y, float w, float h, Color color) {
        drawCircle(x + h / 2, y + h / 2, h / 2, color);
        drawCircle(x + w - h / 2, y + h / 2, h / 2, color);
        drawRect(x + h / 2, y, w - h, h, color);
    }

    public void drawRoundedRect(Rectangle zone, Color color) {
        drawRoundedRect(zone.x, zone.y, zone.width, zone.height, color);
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

    public void drawRoundedGauge(Rectangle rectangle, double ratio, Color back, Color front, boolean reversed) {
        float x = rectangle.x + rectangle.height / 2;
        float width = rectangle.width - rectangle.height;
        float radius = rectangle.height / 2;
        drawCircle(x + (reversed ? 0 : width), rectangle.y + radius, radius, back);
        drawGauge(x, rectangle.y, width, rectangle.height, ratio, back, front, reversed);
        drawCircle(x + (reversed ? width : 0), rectangle.y + radius, radius, front);
        drawCircle(x + width * (reversed ? 1 - (float) ratio : (float) ratio), rectangle.y + radius, radius, front);
    }

    public void drawRoundedGauge(Rectangle rectangle, double ratio, Color back, Color front) {
        drawRoundedGauge(rectangle, ratio, back, front, false);
    }

    private void drawGauge(float x, float y, float width, float height,
                           double ratio, Color back, Color front, boolean reversed) {
        drawRect(x, y, width, height, back);
        drawRect(x + (reversed ? width * (1 - (float) ratio) : 0), y, (float) ratio * width, height, front);
    }

    public void drawGauge(Rectangle rectangle, double ratio, Color back, Color front, boolean reversed) {
        drawGauge(rectangle.x, rectangle.y, rectangle.width, rectangle.height, ratio, back, front, reversed);
    }

    public void drawGauge(Rectangle rectangle, double ratio, Color back, Color front) {
        drawGauge(rectangle, ratio, back, front, false);
    }

    @Override
    public void dispose() {
        rd.dispose();
    }
}
