package com.mygdx.kaps.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.kaps.renderer.Renderable;
import com.mygdx.kaps.renderer.ShapeRendererAdapter;
import com.mygdx.kaps.renderer.SpriteRendererAdapter;
import com.mygdx.kaps.renderer.TextRendererAdaptor;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameView implements Renderable {
    private static class Dimensions {
        private final Rectangle gridZone;
        private final Rectangle gridTile;
        private final Rectangle timeBar;
        private final Rectangle sidekickZone;
        private final Rectangle sidekickGauge1;
        private final Rectangle sidekickGauge2;
        private final Rectangle sidekick1;
        private final Rectangle sidekick2;
        private final Rectangle infoZone;
        private final Rectangle nextBox;
        private final Level level;

        private Dimensions(Level lvl, float screenWidth, float screenHeight) {
            Objects.requireNonNull(lvl);
            Rectangle screen = new Rectangle(0, 0, screenWidth, screenHeight);
            float topSpaceHeight = screenHeight * .8f;
            float gridHeight = topSpaceHeight * .9f;
            float tileSize = gridHeight / lvl.getGrid().getHeight();
            float gridWidth = lvl.getGrid().getWidth() * tileSize;
            float timeBarHeight = (topSpaceHeight - gridHeight) / 4;
            float topSpaceMargin = (topSpaceHeight - gridHeight - timeBarHeight) / 3;
            float infoZoneHeight = (screen.height - topSpaceHeight) / 2;
            float sidekickSize = infoZoneHeight * 3 / 4;
            float nextBoxSize = infoZoneHeight * 5 / 4;

            level = lvl;
            gridZone = new Rectangle((screen.width - gridWidth) / 2, topSpaceMargin, gridWidth, gridHeight);
            gridTile = new Rectangle(0, 0, tileSize, tileSize);
            timeBar = new Rectangle((screen.width - gridWidth) / 2, gridHeight + 2 * topSpaceMargin, gridWidth, timeBarHeight);
            sidekickZone = new Rectangle(0, topSpaceHeight, screen.width, infoZoneHeight);
            sidekickGauge1 = new Rectangle(sidekickZone.x, sidekickZone.y, sidekickZone.width / 2, sidekickZone.height);
            sidekickGauge2 = new Rectangle(sidekickZone.x + sidekickZone.width / 2, sidekickZone.y, sidekickZone.width / 2, sidekickZone.height);
            sidekick1 = new Rectangle(sidekickSize / 8, topSpaceHeight + sidekickSize / 8, sidekickSize, sidekickSize);
            sidekick2 = new Rectangle(screen.width - sidekickSize * 9 / 8, topSpaceHeight + sidekickSize / 8, sidekickSize, sidekickSize);
            infoZone = new Rectangle(0, topSpaceHeight + infoZoneHeight, screen.width, infoZoneHeight);
            nextBox = new Rectangle((screen.width - nextBoxSize) / 2, screen.height - nextBoxSize, nextBoxSize, nextBoxSize);
        }

        private Rectangle tileAt(Coordinates coordinates) {
            return tileAt(coordinates.x, coordinates.y);
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
    private final TextRendererAdaptor tra = new TextRendererAdaptor(spra, 16, Color.WHITE);
    private final Dimensions dimensions;
    private final Level model;

    public GameView(Level lvl) {
        Objects.requireNonNull(lvl);
        dimensions = new Dimensions(lvl, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        model = lvl;
    }

    private void renderGauge(Rectangle rectangle, double ratio, Color back, Color front) {
        sra.drawRect(rectangle, back);
        sra.drawRect(new Rectangle(rectangle.x, rectangle.y, (float) ratio * rectangle.width, rectangle.height), front);
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
              model.getGrid().get(x, y).ifPresent(o -> spra.render(o.getSprite(), dimensions.tileAt(x, y)));
          })
        );
        renderGauge(
          dimensions.timeBar, model.refreshingProgression(), new Color(.3f, .3f, .4f, .5f), new Color(.3f, .3f, .4f, 1f)
        );
    }

    private void renderCapsulePart(CapsulePart part, float alpha) {
        spra.render(part.getSprite(), dimensions.tileAt(part.coordinates()), alpha);
    }

    private void renderCapsulePart(CapsulePart part) {
        spra.render(part.getSprite(), dimensions.tileAt(part.coordinates()));
    }

    private void renderCapsule(Capsule caps, Rectangle zone) {
        caps.applyForEach(
          p -> spra.render(p.getSprite(), zone.x, zone.y + zone.height / 4, zone.width / 2, zone.height / 2),
          p -> spra.render(p.getSprite(), zone.x + zone.width / 2, zone.y + zone.height / 4, zone.width / 2, zone.height / 2)
        );
    }

    private void renderFallingCapsules() {
        model.controlledCapsules().forEach(c -> {
            c.preview().ifPresent(prev -> prev.applyToBoth(p -> renderCapsulePart(p, .5f)));
            c.applyToBoth(this::renderCapsulePart);
        });
    }

    private void renderPoppingObjects() {
        model.poppingObjects().forEach(c -> spra.render(c.getPoppingSprite(), dimensions.tileAt(c.coordinates())));
    }

    private void renderUpcoming() {
        Rectangle nextBox = dimensions.nextBox;
        sra.drawCircle(dimensions.nextBox.x + dimensions.nextBox.width / 2, dimensions.nextBox.y + dimensions.nextBox.height,
          dimensions.nextBox.width, new Color(.45f, .45f, .6f, 1f));
        renderCapsule(model.upcoming().get(0), nextBox);
        tra.drawText("NEXT", nextBox.x, nextBox.y + nextBox.height * 7 / 8, nextBox.width, nextBox.height / 8);
    }

    private void renderSidekicks() {
        var sidekicks = IntStream.range(0, 2)
          .mapToObj(model::getSidekick)
          .collect(Collectors.toUnmodifiableList());
        renderGauge(dimensions.sidekickGauge1, 0, sidekicks.get(0).getColor().value(.3f), sidekicks.get(0).getColor().value(.3f));
        renderGauge(dimensions.sidekickGauge2, 0, sidekicks.get(1).getColor().value(.3f), sidekicks.get(1).getColor().value(.3f));
        spra.render(sidekicks.get(0).getFlippedSprite(), dimensions.sidekick1);
        spra.render(sidekicks.get(1).getSprite(), dimensions.sidekick2);
    }

    @Override
    public void render() {
        renderLayout();
        renderGrid();
        renderFallingCapsules();
        renderPoppingObjects();
        renderSidekicks();
        renderUpcoming();
    }

    public void dispose() {
        spra.dispose();
        sra.dispose();
    }
}
