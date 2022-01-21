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

        private static Rectangle center(Rectangle rectangle) {
            return new Rectangle(rectangle.x + rectangle.width/2, rectangle.y+ rectangle.height/2, 0,0);
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

        private Rectangle tileAt(int x, int y, float scale) {
            var rect = tileAt(x, y);
            return new Rectangle(
              rect.x - rect.width * (scale - 1) / 2,
              rect.y - rect.height * (scale - 1) / 2,
              rect.width * scale,
              rect.height * scale
            );
        }

        private Rectangle tileAt(Coordinates coordinates, float scale) {
            return tileAt(coordinates.x, coordinates.y, scale);
        }
    }

    private enum Font {
        LITTLE, MEDIUM, MEDIUM_GREY, BIG, BIG_GREY
    }

    private final SpriteRendererAdapter spr = new SpriteRendererAdapter();
    private final ShapeRendererAdapter sr = new ShapeRendererAdapter();
    private final Map<Font, TextRendererAdaptor> tr = new HashMap<>();
    private final SpriteData spriteData = new SpriteData();
    private final Dimensions dimensions;
    private final Level model;

    public GameView(Level lvl) {
        model = Objects.requireNonNull(lvl);
        dimensions = new Dimensions(model, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        tr.put(Font.LITTLE, new TextRendererAdaptor(spr, 16, Color.WHITE));
        tr.put(Font.MEDIUM, new TextRendererAdaptor(spr, 24, Color.WHITE));
        tr.put(Font.BIG, new TextRendererAdaptor(spr, 32, Color.WHITE));
        tr.put(Font.MEDIUM_GREY, new TextRendererAdaptor(spr, 20, new Color(.8f, .8f, .9f, 1)));
        tr.put(Font.BIG_GREY, new TextRendererAdaptor(spr, 32, new Color(.7f, .7f, .8f, 1)));
    }

    private void renderLayout() {
        Gdx.gl.glClearColor(.1f, .1f, .15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sr.drawRect(dimensions.infoZone, new Color(.35f, .35f, .45f, 1f));
    }

    private void renderGrid() {
        model.getGrid().forEachTile((x, y) -> {
            sr.drawRect(
              dimensions.tileAt(x, y),
              x % 2 == y % 2 ? new Color(.225f, .225f, .325f, 1) : new Color(.25f, .25f, .35f, 1)
            );
            model.getGrid().get(x, y).ifPresent(o -> spr.render(o.getSprite(spriteData), dimensions.tileAt(x, y)));
        });
        sr.drawRoundedGauge(
          dimensions.timeBar, model.refreshingProgression(), new Color(.2f, .2f, .3f, 1f), new Color(.3f, .3f, .4f, 1f)
        );
    }

    private void renderCapsulePart(CapsulePart part, float alpha) {
        spr.render(part.getSprite(spriteData), dimensions.tileAt(part.coordinates()), alpha);
    }

    private void renderCapsulePart(CapsulePart part) {
        spr.render(part.getSprite(spriteData), dimensions.tileAt(part.coordinates()));
    }

    private void renderCapsule(Capsule caps, Rectangle zone) {
        caps.applyForEach(
          p -> spr.render(p.getSprite(spriteData), zone.x, zone.y + zone.height / 4, zone.width / 2, zone.height / 2),
          p -> spr.render(p.getSprite(spriteData), zone.x + zone.width / 2, zone.y + zone.height / 4, zone.width / 2,
            zone.height / 2)
        );
    }

    private void renderFallingCapsules() {
        model.controlledCapsules().forEach(c -> {
            c.preview().ifPresent(prev -> prev.applyToBoth(p -> renderCapsulePart(p, .5f)));
            c.applyToBoth(this::renderCapsulePart);
        });
    }

    private void renderUpcoming() {
        Rectangle nextBox = dimensions.nextBox;
        sr.drawCircle(dimensions.nextBox.x + dimensions.nextBox.width / 2, dimensions.nextBox.y + dimensions.nextBox.height,
          dimensions.nextBox.width, new Color(.45f, .45f, .6f, 1f));
        renderCapsule(model.upcoming().get(0), nextBox);
        tr.get(Font.MEDIUM).drawTextWithShadow("NEXT", nextBox.x, nextBox.y + nextBox.height * 7 / 8, nextBox.width, nextBox.height / 8);
    }

    private void renderSidekicks() {
        IntStream.range(0, 2).forEach(n -> {
            sr.drawRect(dimensions.sidekickZones.get(n).zone, model.getSidekick(n).color().value(.5f));
            model.getSidekick(n).ifActiveElse(s -> {
                sr.drawRoundedRect(dimensions.sidekickZones.get(n).bubble, Color.WHITE);
                tr.get(Font.BIG_GREY).drawText(s.currentMana() + "    ", dimensions.sidekickZones.get(n).bubble);
                tr.get(Font.MEDIUM_GREY).drawText("      /" + s.maxMana(), dimensions.sidekickZones.get(n).bubble);
                sr.drawRoundedGauge(
                  dimensions.sidekickZones.get(n).gauge,
                  Math.min(1, model.getSidekick(n).gaugeRatio()),
                  new Color(.2f, .2f, .25f, 1),
                  model.getSidekick(n).color().value(),
                  n > 0
                );
            }, s -> {
                sr.drawArc(dimensions.sidekickZones.get(n).cooldown, 270, 360 * (float) s.gaugeRatio(), new Color(1, 1, 1, .3f));
                tr.get(Font.BIG).drawText(s.turnsLeft() + "", dimensions.sidekickZones.get(n).cooldown);
                tr.get(Font.LITTLE).drawText("turns", dimensions.sidekickZones.get(n).cooldownText);
            });
            spr.render(spriteData.getSidekick(model.getSidekick(n), n == 0).getCurrentSprite(), dimensions.sidekickZones.get(n).head);
        });
    }

    private void renderParticles() {
        model.visualParticles().getParticleEffects().forEach(
          p -> spr.render(p.getSprite(), dimensions.tileAt(p.coordinates(), p.getScale()))
        );
        model.visualParticles().getManaParticles().forEach(p -> {
            float x = lerp(
              Dimensions.center(dimensions.tileAt(p.coordinates())).x,
              Dimensions.center(dimensions.sidekickZones.get(p.getTarget()).head).x, p.ratio()
            );
            float y = lerp(
              Dimensions.center(dimensions.tileAt(p.coordinates())).y,
              Dimensions.center(dimensions.sidekickZones.get(p.getTarget()).head).y, p.ratio()
            );
            sr.drawCircle(x, y, 15, p.color().value(0.4f));
            sr.drawCircle(x, y, 5, p.color().value());
        });
    }

    private float lerp(float from, float to, double ratio) {
        float easeRatio = (float) (ratio * ratio * (3f - 2f * ratio));
        return from + (to - from) * easeRatio;
    }

    void updateSprites() {
        spriteData.updateSprites();
    }

    public void render() {
        renderLayout();
        renderGrid();
        renderFallingCapsules();
        renderSidekicks();
        renderUpcoming();
        renderParticles();
    }

    public void dispose() {
        spr.dispose();
        sr.dispose();
        tr.values().forEach(TextRendererAdaptor::dispose);
    }
}
