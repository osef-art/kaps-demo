package com.mygdx.kaps.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.kaps.renderer.Renderable;
import com.mygdx.kaps.renderer.ShapeRendererAdapter;
import com.mygdx.kaps.renderer.SpriteRendererAdapter;

import java.util.stream.IntStream;

public class GameView implements Renderable {
    private static class Dimensions {
        private final Rectangle gridZone;
        private final Rectangle gridTile;
        private final Rectangle sidekickZone;
        private final Rectangle infoZone;
        private final Level level;

        private Dimensions(Level lvl, float screenWidth, float screenHeight) {
            Rectangle screen = new Rectangle(0, 0, screenWidth, screenHeight);
            float topSpaceHeight = screenHeight * 0.8f;
            float gridHeight = topSpaceHeight * 0.9f;
            float tileSize = gridHeight / lvl.getGrid().getHeight();
            float gridWidth = lvl.getGrid().getWidth() * tileSize;
            float infoZoneHeight = (screen.height - topSpaceHeight) / 2;

            level = lvl;
            gridZone = new Rectangle((screen.width - gridWidth) / 2, (topSpaceHeight - gridHeight) / 2, gridWidth, gridHeight);
            gridTile = new Rectangle(0, 0, tileSize, tileSize);
            sidekickZone = new Rectangle(0, topSpaceHeight, screen.width, infoZoneHeight);
            infoZone = new Rectangle(0, topSpaceHeight + infoZoneHeight, screen.width, infoZoneHeight);
        }

        private Rectangle tileAt(int x, int y) {
            return new Rectangle(
              gridZone.x + x * gridTile.width,
              gridZone.y + ((level.getGrid().getHeight() - 1) - y) * gridTile.height,
              gridTile.width,
              gridTile.height
            );
        }
    }

    private final SpriteRendererAdapter spra = new SpriteRendererAdapter();
    private final ShapeRendererAdapter sra = new ShapeRendererAdapter();
    private final Dimensions dimensions;
    private final Level model;

    public GameView(Level model) {
        this.model = model;
        dimensions = new Dimensions(model, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void renderLayout() {
        sra.drawRect(dimensions.sidekickZone, new Color(.3f, .3f, .375f, 1f));
        sra.drawRect(dimensions.infoZone, new Color(.35f, .35f, .45f, 1f));
    }

    private void renderGrid() {
        IntStream.range(0, model.getGrid().getWidth()).forEach(
          x -> IntStream.range(0, model.getGrid().getHeight()).forEach(y -> {
              sra.drawRect(
                dimensions.tileAt(x, y),
                x % 2 == y % 2 ? new Color(.2f, .2f, .3f, 1) : new Color(.175f, .175f, .275f, 1)
              );
              model.getGrid().get(x, y).ifPresent(c -> spra.render(c.getSprite(), dimensions.tileAt(x, y)));
          })
        );
    }

    private void renderCapsule(Capsule caps) {
        spra.render(caps.getSprite(), dimensions.tileAt(caps.coordinates().x, caps.coordinates().y));
    }

    private void renderFallingCapsules() {
        model.fallingCapsules().forEach(g -> g.forEachCapsule(this::renderCapsule));
    }

    @Override
    public void render() {
        renderLayout();
        renderGrid();
        renderFallingCapsules();
    }

    public void dispose() {
        spra.dispose();
        sra.dispose();
    }
}
