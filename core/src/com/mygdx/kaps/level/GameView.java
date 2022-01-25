package com.mygdx.kaps.level;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.CapsulePart;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.renderer.ShapeRendererAdapter;
import com.mygdx.kaps.renderer.SpriteData;
import com.mygdx.kaps.renderer.SpriteRendererAdapter;
import com.mygdx.kaps.renderer.TextRendererAdaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameView extends ApplicationAdapter {
    private static class Dimensions {
        private static class SidekickZone {
            private enum Zone {ZONE, HEAD, GAUGE, BUBBLE, COOLDOWN, COOLDOWN_TXT}

            private final boolean flipped;
            private final Map<Zone, Rectangle> zones = new HashMap<>();

            private SidekickZone(SidekickZone sdkZone, Dimensions dimensions) {
                sdkZone.zones.forEach((z, rect) -> zones.put(z, dimensions.symmetrical(rect)));
                this.flipped = false;
            }

            private SidekickZone(Rectangle zone, Rectangle head, Rectangle gauge,
                                 Rectangle bubble, Rectangle cooldown, Rectangle cooldownText) {
                zones.put(Zone.COOLDOWN_TXT, cooldownText);
                zones.put(Zone.COOLDOWN, cooldown);
                zones.put(Zone.BUBBLE, bubble);
                zones.put(Zone.GAUGE, gauge);
                zones.put(Zone.HEAD, head);
                zones.put(Zone.ZONE, zone);
                this.flipped = true;
            }

            private Rectangle get(Zone zone) {
                return zones.get(zone);
            }
        }

        private final Rectangle screen;
        private final Rectangle gridZone;
        private final List<List<Rectangle>> gridTiles;
        private final Rectangle timeBar;
        private final Map<SidekickId, SidekickZone> sidekickZones = new HashMap<>();
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

            sidekickZones.put(lvl.getSidekicks().get(0).id(), new SidekickZone(
              new Rectangle(0, topSpaceHeight, screen.width / 2, infoZoneHeight),
              new Rectangle(padding, topSpaceHeight + padding, sidekickSize, sidekickSize),
              new Rectangle(screen.width * 3 / 16, topSpaceHeight + infoZoneHeight / 2, screen.width / 4, 15),
              new Rectangle(sidekickSize + padding * 2, topSpaceHeight - padding, screen.width / 5, infoZoneHeight * 2 / 5),
              new Rectangle(screen.width * 5 / 16, topSpaceHeight + padding, sidekickSize, sidekickSize),
              new Rectangle(screen.width * 5 / 16, topSpaceHeight + infoZoneHeight / 2, sidekickSize, sidekickSize / 2)
            ));
            sidekickZones.put(lvl.getSidekicks().get(1).id(),
              new SidekickZone(sidekickZones.get(lvl.getSidekicks().get(0).id()), this)
            );
            infoZone = new Rectangle(0, topSpaceHeight + infoZoneHeight, screen.width, infoZoneHeight);
            nextBox = new Rectangle((screen.width - nextBoxSize) / 2, screen.height - nextBoxSize, nextBoxSize, nextBoxSize);
        }

        private static Rectangle lerp(Rectangle from, Rectangle to, double ratio) {
            return new Rectangle(
              Utils.lerp(from.x, to.x, ratio),
              Utils.lerp(from.y, to.y, ratio),
              Utils.lerp(from.width, to.width, ratio),
              Utils.lerp(from.height, to.height, ratio)
            );
        }

        private static Rectangle center(Rectangle rectangle) {
            return new Rectangle(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2, 0, 0);
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
        dimensions.sidekickZones.forEach(
          (sdk, zone) -> sr.drawRect(zone.get(Dimensions.SidekickZone.Zone.ZONE), sdk.color().value(.5f))
        );
        model.getGrid().forEachTile((x, y) -> sr.drawRect(
          dimensions.tileAt(x, y),
          x % 2 == y % 2 ? new Color(.225f, .225f, .325f, 1) : new Color(.25f, .25f, .35f, 1)
        ));
    }

    private void renderSidekickFocus() {
        model.getSidekicks().stream()
          .filter(Sidekick::isAttacking)
          .findFirst().ifPresent(sidekick -> sidekick.ifActive(sdk -> {
              sr.drawRect(dimensions.screen, new Color(0, 0, 0, .5f));
              sr.drawRect(dimensions.screen, sdk.color().value(.2f));
              renderSidekick(sdk);
          }));
    }

    private void renderGrid() {
        model.getGrid().stack().forEach(o -> spr.render(o.getSprite(spriteData), dimensions.tileAt(o.coordinates())));
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

    private void renderSidekick(Sidekick sdk) {
        var sdkZone = dimensions.sidekickZones.get(sdk.id());
        sdk.ifActiveElse(s -> {
            sr.drawRoundedRect(sdkZone.get(Dimensions.SidekickZone.Zone.BUBBLE), Color.WHITE);
            tr.get(Font.BIG_GREY).drawText(s.currentMana() + "    ", sdkZone.get(Dimensions.SidekickZone.Zone.BUBBLE));
            tr.get(Font.MEDIUM_GREY).drawText("      /" + s.maxMana(), sdkZone.get(Dimensions.SidekickZone.Zone.BUBBLE));
            sr.drawRoundedGauge(
              sdkZone.get(Dimensions.SidekickZone.Zone.GAUGE),
              Math.min(1, sdk.gaugeRatio()),
              new Color(.2f, .2f, .25f, 1),
              sdk.color().value(),
              !sdkZone.flipped
            );
        }, s -> {
            sr.drawArc(sdkZone.get(Dimensions.SidekickZone.Zone.COOLDOWN), 270, 360 * (float) s.gaugeRatio(), new Color(1, 1, 1, .3f));
            tr.get(Font.BIG).drawText(s.turnsLeft() + "", sdkZone.get(Dimensions.SidekickZone.Zone.COOLDOWN));
            tr.get(Font.LITTLE).drawText("turns", sdkZone.get(Dimensions.SidekickZone.Zone.COOLDOWN_TXT));
        });
        spr.render(spriteData.getSidekick(sdk.id(), sdkZone.flipped).getCurrentSprite(), sdkZone.get(Dimensions.SidekickZone.Zone.HEAD));
    }

    private void renderParticles() {
        model.visualParticles().getParticleEffects().forEach(
          p -> spr.render(p.getSprite(), dimensions.tileAt(p.coordinates(), p.getScale()))
        );
        model.visualParticles().getManaParticles().forEach(p -> {
            Rectangle center = Dimensions.center(Dimensions.lerp(
              dimensions.tileAt(p.coordinates()),
              dimensions.sidekickZones.get(p.getTarget()).get(Dimensions.SidekickZone.Zone.HEAD), p.ratio()
            ));
            sr.drawCircle(center.x, center.y, 15, p.getTarget().color.value(.4f));
            sr.drawCircle(center.x, center.y, 5, p.getTarget().color.value());
        });
    }

    private void renderEndMessage() {
        model.gameEndManager().ifChecked(model, c -> tr.get(Font.BIG).drawText(c.getMessage(), dimensions.screen));
    }

    void updateSprites() {
        spriteData.updateSprites();
    }

    public void render() {
        renderLayout();
        renderUpcoming();
        model.getSidekicks().forEach(this::renderSidekick);
        renderSidekickFocus();
        renderGrid();
        renderFallingCapsules();
        renderParticles();
        renderEndMessage();
    }

    public void dispose() {
        spr.dispose();
        sr.dispose();
        tr.values().forEach(TextRendererAdaptor::dispose);
    }
}
