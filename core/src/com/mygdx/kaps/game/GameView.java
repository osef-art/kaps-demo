package com.mygdx.kaps.game;

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
        private final Rectangle sidekickZone;
        private final Rectangle infoZone;
        private final Rectangle gridTile;

        private Dimensions(GameScene model, float screenWidth, float screenHeight) {
            Rectangle screen = new Rectangle(0, 0, screenWidth, screenHeight);
            float topSpaceHeight = screenHeight * 0.8f;
            float gridHeight = topSpaceHeight * 0.9f;
            float tileSize = gridHeight / model.getGrid().getHeight();
            float gridWidth = model.getGrid().getWidth() * tileSize;
            float infoZoneHeight = (screen.height - topSpaceHeight) / 2;

            gridZone = new Rectangle((screen.width - gridWidth) / 2, (topSpaceHeight - gridHeight) / 2, gridWidth, gridHeight);
            gridTile = new Rectangle(0, 0, tileSize, tileSize);
            sidekickZone = new Rectangle(0, topSpaceHeight, screen.width, infoZoneHeight);
            infoZone = new Rectangle(0, topSpaceHeight + infoZoneHeight, screen.width, infoZoneHeight);
        }
    }

    private final SpriteRendererAdapter spra = new SpriteRendererAdapter();
    private final ShapeRendererAdapter sra = new ShapeRendererAdapter();
    private final Dimensions dimensions;
    private final GameScene model;

    public GameView(GameScene model) {
        this.model = model;
        dimensions = new Dimensions(model, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void renderLayout() {
        sra.drawRect(dimensions.sidekickZone, new Color(.3f, .3f, .375f, 1f));
        sra.drawRect(dimensions.infoZone, new Color(.35f, .35f, .45f, 1f));
    }

    private void renderGrid() {
        var gridZone = dimensions.gridZone;
        var tile = dimensions.gridTile;
        IntStream.range(0, model.getGrid().getWidth()).forEach(
          x -> IntStream.range(0, model.getGrid().getHeight()).forEach(
            y -> sra.drawRect(
              new Rectangle(gridZone.x + x * tile.width, gridZone.y + y * tile.height, tile.width, tile.height),
              x % 2 == y % 2 ? new Color(.15f, .15f, .225f, 1) : new Color(.175f, .175f, .25f, 1)
            )
          )
        );
    }

    @Override
    public void render() {
        renderLayout();
        renderGrid();
    }

    public void dispose() {
        spra.dispose();
        sra.dispose();
    }
}
