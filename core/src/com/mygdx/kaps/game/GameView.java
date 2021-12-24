package com.mygdx.kaps.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.kaps.renderer.Renderable;
import com.mygdx.kaps.renderer.ShapeRendererAdapter;
import com.mygdx.kaps.renderer.SpriteRendererAdapter;

public class GameView implements Renderable {
    private static class Dimensions {
        private final Rectangle gridZone;
        private final Rectangle sidekickZone;
        private final Rectangle infoZone;

        private Dimensions(GameScene model, float screenWidth, float screenHeight) {
            Rectangle screen = new Rectangle(0, 0, screenWidth, screenHeight);
            float topSpaceHeight = screenHeight * 0.7f;
            float gridHeight = topSpaceHeight * 0.9f;
            float tileSize = gridHeight / model.getGrid().getDimensions().y;
            float gridWidth = model.getGrid().getDimensions().x * tileSize;
            float infoZoneHeight = (screen.height - topSpaceHeight) / 2;

//            System.out.println(topSpaceHeight + ", " + gridHeight + ", " + tileSize + ", " + gridWidth + ", " + infoZoneHeight);
            System.out.println(model.getGrid().getDimensions());

            gridZone = new Rectangle((screen.width - gridWidth) / 2, (topSpaceHeight - gridHeight )/ 2, gridWidth, gridHeight);
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
        sra.drawRect(dimensions.gridZone, new Color(.2f, .2f, .25f, 1f));
        sra.drawRect(dimensions.sidekickZone, new Color(.3f, .3f, .35f, 1f));
        sra.drawRect(dimensions.infoZone, new Color(.4f, .4f, .45f, 1f));
    }

    private void renderGrid() {

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
