package com.mygdx.kaps.level;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.kaps.level.gridobject.CapsulePart;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.renderer.ShapeRendererAdapter;
import com.mygdx.kaps.renderer.SpriteData;
import com.mygdx.kaps.renderer.SpriteRendererAdapter;
import com.mygdx.kaps.renderer.TextRendererAdaptor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameView extends ApplicationAdapter {
    private static class Dimensions {
        private static class SidekickZone {
            private final Rectangle zone;
            private final Rectangle head;
            private final Rectangle gauge;
            private final Rectangle bubble;
            private final Rectangle cooldown;
            private final Rectangle cooldownText;

            private SidekickZone(Rectangle zone, Rectangle head, Rectangle gauge, Rectangle bubble, Rectangle cooldown, Rectangle cooldownText) {
                this.zone = zone;
                this.head = head;
                this.gauge = gauge;
                this.bubble = bubble;
                this.cooldown = cooldown;
                this.cooldownText = cooldownText;
            }
        }

        private final Rectangle screen;
        private final Rectangle gridZone;
        private final List<List<Rectangle>> gridTiles;
        private final Rectangle timeBar;
        private final List<SidekickZone> sidekickZones = new ArrayList<>();
        private final Rectangle infoZone;
        private final Rectangle nextBox;
        private final Level level;

        private Dimensions(Level lvl, float screenWidth, float screenHeight) {
            Objects.requireNonNull(lvl);
            screen = new Rectangle(0, 0, screenWidth, screenHeight);
            float topSpaceHeight = screenHeight * .8f;
            float gridHeight = topSpaceHeight * .9f;
            float tileSize = gridHeight / lvl.getGrid().getHeight();
            float gridWidth = lvl.getGrid().getWidth() * tileSize;
            float timeBarHeight = (topSpaceHeight - gridHeight) / 4;
            float topSpaceMargin = (topSpaceHeight - gridHeight - timeBarHeight) / 3;
            float infoZoneHeight = (screenHeight - topSpaceHeight) / 2;
            float sidekickSize = infoZoneHeight * 3 / 4;
            float nextBoxSize = infoZoneHeight * 5 / 4;
            float padding = infoZoneHeight * 3 / 32;

            level = lvl;
            gridZone = new Rectangle((screen.width - gridWidth) / 2, topSpaceMargin, gridWidth, gridHeight);
            gridTiles = IntStream.range(0, lvl.getGrid().getHeight()).mapToObj(
                y -> IntStream.range(0, lvl.getGrid().getWidth()).mapToObj(x -> new Rectangle(
                  gridZone.x + x * tileSize,
                  gridZone.y + ((level.getGrid().getHeight() - 1) - y) * tileSize,
                  tileSize,
                  tileSize
                )).collect(Collectors.toUnmodifiableList()))
              .collect(Collectors.toUnmodifiableList());
            timeBar = new Rectangle((screen.width - gridWidth) / 2, gridHeight + 2 * topSpaceMargin, gridWidth, timeBarHeight);

            sidekickZones.add(new SidekickZone(
              new Rectangle(0, topSpaceHeight, screen.width / 2, infoZoneHeight),
              new Rectangle(padding, topSpaceHeight + padding, sidekickSize, sidekickSize),
              new Rectangle(screen.width * 3 / 16, topSpaceHeight + infoZoneHeight / 2, screen.width / 4, 15),
              new Rectangle(sidekickSize + padding * 2, topSpaceHeight - padding, screen.width / 5, infoZoneHeight * 2 / 5),
              new Rectangle(screen.width * 5 / 16, topSpaceHeight + padding, sidekickSize, sidekickSize),
              new Rectangle(screen.width * 5 / 16, topSpaceHeight + infoZoneHeight / 2, sidekickSize, sidekickSize / 2)
            ));
            sidekickZones.add(new SidekickZone(
              symmetrical(sidekickZones.get(0).zone),
              symmetrical(sidekickZones.get(0).head),
              symmetrical(sidekickZones.get(0).gauge),
              symmetrical(sidekickZones.get(0).bubble),
              symmetrical(sidekickZones.get(0).cooldown),
              symmetrical(sidekickZones.get(0).cooldownText)
            ));
            infoZone = new Rectangle(0, topSpaceHeight + infoZoneHeight, screen.width, infoZoneHeight);
            nextBox = new Rectangle((screen.width - nextBoxSize) / 2, screen.height - nextBoxSize, nextBoxSize, nextBoxSize);
        }

        private Rectangle symmetrical(Rectangle rectangle) {
            return new Rectangle(screen.width - rectangle.x - rectangle.width, rectangle.y, rectangle.width, rectangle.height);
        }

        private Rectangle tileAt(Coordinates coordinates) {
            return tileAt(coordinates.x, coordinates.y);
        }

        private Rectangle tileAt(int x, int y) {
            return gridTiles.get(y).get(x);
        }

        private Rectangle tileAt(Coordinates coordinates, float scale) {
            var rect = tileAt(coordinates);
            return new Rectangle(
              rect.x - rect.width * (scale-1) / 2,
              rect.y - rect.height * (scale-1) / 2,
              rect.width * scale,
              rect.height * scale
            );
        }
    }

    private enum Font {
        LITTLE, MEDIUM, MEDIUM_GREY, BIG, BIG_GREY
    }

    private final SpriteRendererAdapter spra = new SpriteRendererAdapter();
    private final ShapeRendererAdapter sra = new ShapeRendererAdapter();
    private final Map<Font, TextRendererAdaptor> tra = new HashMap<>();
    private final SpriteData spriteData = new SpriteData();
    private final Dimensions dimensions;
    private final Level model;

    public GameView(Level lvl) {
        model = Objects.requireNonNull(lvl);
        dimensions = new Dimensions(model, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        tra.put(Font.LITTLE, new TextRendererAdaptor(spra, 16, Color.WHITE));
        tra.put(Font.MEDIUM, new TextRendererAdaptor(spra, 24, Color.WHITE));
        tra.put(Font.BIG, new TextRendererAdaptor(spra, 32, Color.WHITE));
        tra.put(Font.MEDIUM_GREY, new TextRendererAdaptor(spra, 20, new Color(.8f, .8f, .9f, 1)));
        tra.put(Font.BIG_GREY, new TextRendererAdaptor(spra, 32, new Color(.7f, .7f, .8f, 1)));
    }

    private void renderLayout() {
        Gdx.gl.glClearColor(.1f, .1f, .15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sra.drawRect(dimensions.infoZone, new Color(.35f, .35f, .45f, 1f));
    }

    private void renderGrid() {
        model.getGrid().forEachTile((x, y) -> {
            sra.drawRect(
              dimensions.tileAt(x, y),
              x % 2 == y % 2 ? new Color(.225f, .225f, .325f, 1) : new Color(.25f, .25f, .35f, 1)
            );
            model.getGrid().get(x, y).ifPresent(o -> spra.render(o.getSprite(spriteData), dimensions.tileAt(x, y)));
        });
        sra.drawRoundedGauge(
          dimensions.timeBar, model.refreshingProgression(), new Color(.2f, .2f, .3f, 1f), new Color(.3f, .3f, .4f, 1f)
        );
    }

    private void renderCapsulePart(CapsulePart part, float alpha) {
        spra.render(part.getSprite(spriteData), dimensions.tileAt(part.coordinates()), alpha);
    }

    private void renderCapsulePart(CapsulePart part) {
        spra.render(part.getSprite(spriteData), dimensions.tileAt(part.coordinates()));
    }

    private void renderCapsule(Capsule caps, Rectangle zone) {
        caps.applyForEach(
          p -> spra.render(p.getSprite(spriteData), zone.x, zone.y + zone.height / 4, zone.width / 2, zone.height / 2),
          p -> spra.render(p.getSprite(spriteData), zone.x + zone.width / 2, zone.y + zone.height / 4, zone.width / 2,
            zone.height / 2)
        );
    }

    private void renderFallingCapsules() {
        model.controlledCapsules().forEach(c -> {
            c.preview().ifPresent(prev -> prev.applyToBoth(p -> renderCapsulePart(p, .5f)));
            c.applyToBoth(this::renderCapsulePart);
        });
    }

    private void renderPoppingObjects() {
        //        model.poppingObjects().forEach(c -> spra.render(c.getPoppingSprite(), dimensions.tileAt(c.coordinates())));
    }

    private void renderUpcoming() {
        Rectangle nextBox = dimensions.nextBox;
        sra.drawCircle(dimensions.nextBox.x + dimensions.nextBox.width / 2, dimensions.nextBox.y + dimensions.nextBox.height,
          dimensions.nextBox.width, new Color(.45f, .45f, .6f, 1f));
        renderCapsule(model.upcoming().get(0), nextBox);
        tra.get(Font.MEDIUM).drawTextWithShadow("NEXT", nextBox.x, nextBox.y + nextBox.height * 7 / 8, nextBox.width, nextBox.height / 8);
    }

    private void renderSidekicks() {
        IntStream.range(0, 2).forEach(n -> {
            sra.drawRect(dimensions.sidekickZones.get(n).zone, model.getSidekick(n).color().value(.5f));
            model.getSidekick(n).ifActiveElse(s -> {
                sra.drawRoundedRect(dimensions.sidekickZones.get(n).bubble, Color.WHITE);
                tra.get(Font.BIG_GREY).drawText(s.currentMana() + "    ", dimensions.sidekickZones.get(n).bubble);
                tra.get(Font.MEDIUM_GREY).drawText("      /" + s.maxMana(), dimensions.sidekickZones.get(n).bubble);
                sra.drawRoundedGauge(
                  dimensions.sidekickZones.get(n).gauge,
                  Math.min(1, model.getSidekick(n).gaugeRatio()),
                  new Color(.2f, .2f, .25f, 1),
                  model.getSidekick(n).color().value(),
                  n > 0
                );
            }, s -> {
                sra.drawArc(dimensions.sidekickZones.get(n).cooldown, 270, 360 * (float) s.gaugeRatio(), new Color(1, 1, 1, .3f));
                tra.get(Font.BIG).drawText(s.turnsLeft() + "", dimensions.sidekickZones.get(n).cooldown);
                tra.get(Font.LITTLE).drawText("turns", dimensions.sidekickZones.get(n).cooldownText);
            });
        });

        spra.render(model.getSidekick(0).getFlippedSprite(), dimensions.sidekickZones.get(0).head);
        spra.render(model.getSidekick(1).getSprite(), dimensions.sidekickZones.get(1).head);
    }

    private void renderParticles() {
        model.visualParticles().forEach(
          p -> spra.render(p.getSprite(), dimensions.tileAt(p.coordinates(), p.getScale()))
        );
    }

    void updateSprites() {
        spriteData.updateSprites();
    }

    public void render() {
        renderLayout();
        renderGrid();
        renderFallingCapsules();
        renderPoppingObjects();
        renderSidekicks();
        renderUpcoming();
        renderParticles();
    }

    public void dispose() {
        spra.dispose();
        sra.dispose();
        tra.values().forEach(TextRendererAdaptor::dispose);
    }
}
