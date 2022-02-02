package com.mygdx.kaps.level;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.CapsulePart;
import com.mygdx.kaps.level.gridobject.Coordinates;
import com.mygdx.kaps.level.gridobject.Germ;
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
            private enum Zone {ZONE, HEAD, GAUGE, BUBBLE, COOLDOWN, COOLDOWN_TXT}

            private final Map<Zone, Rectangle> zones = new HashMap<>();
            private final boolean flipped;

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
        private final List<List<Rectangle>> tileCorners;
        private final Rectangle timeBar;
        private final Map<SidekickId, SidekickZone> sidekickZones = new HashMap<>();
        private final Rectangle scoreZone;
        private final Rectangle infoZone;
        private final Rectangle nextBox;
        private final Rectangle nextCaps;
        private final Rectangle germCountBox;
        private final Rectangle germCountTxt;
        private final Rectangle holdBox;
        private final Rectangle holdCaps;
        private final Level level;

        private Dimensions(Level lvl, float screenWidth, float screenHeight) {
            level = Objects.requireNonNull(lvl);
            screen = new Rectangle(0, 0, screenWidth, screenHeight);
            float topSpaceHeight = screenHeight * .75f;
            float gridHeight = topSpaceHeight * .9f;
            float tileSize = gridHeight / lvl.getGrid().getHeight();
            float cornerSize = tileSize / 3;
            float gridWidth = lvl.getGrid().getWidth() * tileSize;
            float timeBarHeight = (topSpaceHeight - gridHeight) / 4;
            float topSpaceMargin = (topSpaceHeight - gridHeight - timeBarHeight) / 3;
            float infoZoneHeight = (screenHeight - topSpaceHeight) * 2 / 5;
            float sidekickSize = infoZoneHeight * 3 / 4;
            float nextBoxSize = (screenHeight - topSpaceHeight) * 7 / 10;
            float nextHeight = nextBoxSize * 3 / 8;
            float holdHeight = infoZoneHeight * 3 / 4;
            float padding = infoZoneHeight * 3 / 32;

            gridZone = new Rectangle((screen.width - gridWidth) / 2, topSpaceMargin, gridWidth, gridHeight);
            gridTiles = IntStream.range(0, lvl.getGrid().getHeight()).mapToObj(
                y -> IntStream.range(0, lvl.getGrid().getWidth()).mapToObj(x -> new Rectangle(
                  gridZone.x + x * tileSize,
                  gridZone.y + ((level.getGrid().getHeight() - 1) - y) * tileSize,
                  tileSize,
                  tileSize
                )).collect(Collectors.toUnmodifiableList()))
              .collect(Collectors.toUnmodifiableList());
            tileCorners = gridTiles.stream().map(lst -> lst.stream().map(
                r -> new Rectangle(r.x + r.width - cornerSize, r.y + r.height - cornerSize, cornerSize, cornerSize)
              ).collect(Collectors.toUnmodifiableList()))
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
            scoreZone = new Rectangle(0, topSpaceHeight + infoZoneHeight, screen.width, infoZoneHeight);
            infoZone = new Rectangle(0, screen.height - infoZoneHeight / 2, screen.width, infoZoneHeight / 2);
            nextBox = new Rectangle((screen.width - nextBoxSize) / 2, screen.height - nextBoxSize, nextBoxSize, nextBoxSize);
            nextCaps = new Rectangle(screen.width / 2 - nextHeight, nextBox.y + nextHeight / 4, nextHeight * 2, nextHeight);
            holdBox = symmetrical(padding, scoreZone.y + scoreZone.height / 2 - holdHeight / 2, holdHeight * 2, holdHeight);
            holdCaps = scaled(holdBox, .7f);

            germCountTxt = symmetrical(padding, infoZone.y, infoZone.height, infoZone.height);
            germCountBox = new Rectangle(germCountTxt.x - germCountTxt.width, germCountTxt.y, germCountTxt.width, germCountTxt.height);
        }

        private static Rectangle lerp(Rectangle from, Rectangle to, double ratio) {
            return new Rectangle(
              Utils.easeLerp(from.x, to.x, ratio),
              Utils.easeLerp(from.y, to.y, ratio),
              Utils.easeLerp(from.width, to.width, ratio),
              Utils.easeLerp(from.height, to.height, ratio)
            );
        }

        private static Rectangle center(Rectangle rectangle) {
            return new Rectangle(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2, 0, 0);
        }

        private Rectangle symmetrical(float x, float y, float width, float height) {
            return new Rectangle(screen.width - x - width, y, width, height);
        }

        private Rectangle symmetrical(Rectangle rectangle) {
            return symmetrical(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }

        private static Rectangle scaled(Rectangle rectangle, float scale) {
            return new Rectangle(
              rectangle.x - rectangle.width * (scale - 1) / 2,
              rectangle.y - rectangle.height * (scale - 1) / 2,
              rectangle.width * scale,
              rectangle.height * scale
            );
        }

        private Rectangle tileAt(int x, int y) {
            return gridTiles.get(y).get(x);
        }

        private Rectangle tileAt(Coordinates coordinates) {
            return tileAt(coordinates.x, coordinates.y);
        }

        private Rectangle tileCornerAt(int x, int y) {
            return tileCorners.get(y).get(x);
        }

        private Rectangle tileCornerAt(Coordinates coordinates) {
            return tileCornerAt(coordinates.x, coordinates.y);
        }
    }

    private enum Font {
        LITTLE, MEDIUM, MEDIUM_GREY, BIG, BIG_GREY
    }

    private final Map<Font, TextRendererAdaptor> tr = new HashMap<>();
    private final SpriteRendererAdapter spr;
    private final ShapeRendererAdapter sr;
    private final com.mygdx.kaps.level.gridobject.Color mainTheme;
    private final SpriteData spriteData = new SpriteData();
    private final OrthographicCamera camera;
    private final Vector3 camOriginalPos;
    private final Dimensions dimensions;
    private final Level model;

    public GameView(Level lvl) {
        model = Objects.requireNonNull(lvl);
        mainTheme = lvl.getSidekicks().get(0).color();
        dimensions = new Dimensions(model, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        camera.update();
        camOriginalPos = camera.position.cpy();
        sr = new ShapeRendererAdapter(camera);
        spr = new SpriteRendererAdapter(camera);

        tr.put(Font.LITTLE, new TextRendererAdaptor(spr, 16, Color.WHITE));
        tr.put(Font.MEDIUM, new TextRendererAdaptor(spr, 24, Color.WHITE));
        tr.put(Font.BIG, new TextRendererAdaptor(spr, 32, Color.WHITE, mainTheme.value()));
        tr.put(Font.MEDIUM_GREY, new TextRendererAdaptor(spr, 20, new Color(.5f, .5f, .65f, .3f)));
        tr.put(Font.BIG_GREY, new TextRendererAdaptor(spr, 32, new Color(.5f, .5f, .65f, .5f)));
    }

    private void renderLayout() {
        Gdx.gl.glClearColor(.1f, .1f, .15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        model.getGrid().forEachTile((x, y) -> sr.drawRect(
          dimensions.tileAt(x, y),
          x % 2 == y % 2 ? new Color(.275f, .275f, .4f, 1) : new Color(.25f, .25f, .375f, 1)
        ));
        dimensions.sidekickZones.forEach(
          (sdk, zone) -> sr.drawRect(zone.get(Dimensions.SidekickZone.Zone.ZONE), sdk.color().value(.5f))
        );

        // score & next
        sr.drawRect(dimensions.scoreZone, new Color(.35f, .35f, .45f, 1f));
        sr.drawRoundedRect(dimensions.holdBox, new Color(.3f, .3f, .4f, 1f));
        tr.get(Font.MEDIUM_GREY).drawText("HOLD", dimensions.holdBox);

        sr.drawCircle(dimensions.nextBox.x + dimensions.nextBox.width / 2, dimensions.nextBox.y + dimensions.nextBox.height,
          dimensions.nextBox.width, new Color(.45f, .45f, .6f, 1f));

        if (model.getScoreData().currentCombo() > 1)
            tr.get(Font.LITTLE).drawText("x" + model.getScoreData().currentCombo() + " COMBO !",
              50 + new Random().nextInt(3), dimensions.scoreZone.y + 10 + new Random().nextInt(3));
        tr.get(Font.BIG).drawTextWithShadow(String.valueOf(model.getScoreData().totalScore()), 15,
          dimensions.scoreZone.y + dimensions.scoreZone.height / 2);

        // info
        sr.drawRect(dimensions.infoZone, Color.BLACK);
        tr.get(Font.MEDIUM).drawText(model.getLabel(), dimensions.infoZone);
        sr.drawRect(dimensions.infoZone, mainTheme.value(.4f));
        tr.get(Font.MEDIUM).drawText("NEXT", dimensions.nextBox.x, dimensions.infoZone.y - 28,
          dimensions.nextBox.width, 28);

        spr.render(spriteData.getGerm(Germ.GermKind.BASIC, mainTheme).getCurrentSprite(), dimensions.germCountBox);
        tr.get(Font.MEDIUM).drawText(String.valueOf(model.getGermsCount()), dimensions.germCountTxt);
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

    private void renderStack() {
        model.getGrid().stack().forEach(o -> o.ifGermElse(
          germ -> germ.ifHasCooldownElse(cg -> {
                var cdn = cg.coordinates();
                var corner = dimensions.tileCornerAt(cdn);
                if (cg.isAttacking()) sr.drawRect(
                  dimensions.tileAt(cdn),
                  cdn.x % 2 == cdn.y % 2 ? new Color(.225f, .225f, .325f, 1) : new Color(.25f, .25f, .35f, 1)
                );
                sr.drawArc(corner, 270, 360 * (float) cg.gaugeRatio(), new Color(1, 1, 1, .5f));
                tr.get(Font.LITTLE).drawText(cg.turnsLeft() + "", corner);
            },
            g -> spr.render(g.getSprite(spriteData), dimensions.tileAt(g.coordinates()))
          ), c -> spr.render(c.getSprite(spriteData), dimensions.tileAt(c.coordinates()))));
        sr.drawRoundedGauge(
          dimensions.timeBar, model.refreshingProgression(), new Color(.2f, .2f, .3f, 1f), new Color(.4f, .4f, .5f, 1f)
        );
    }

    private void renderCapsulePart(CapsulePart part, float alpha) {
        spr.render(part.getSprite(spriteData), dimensions.tileAt(part.coordinates()), alpha);
    }

    private void renderCapsulePart(CapsulePart part) {
        spr.render(part.getSprite(spriteData), dimensions.tileAt(part.coordinates()));
    }

    private void renderCapsule(Capsule caps, Rectangle zone, float alpha) {
        caps.applyForEach(
          p -> spr.render(p.getSprite(spriteData), zone.x, zone.y, zone.width / 2, zone.height, alpha),
          p -> spr.render(p.getSprite(spriteData), zone.x + zone.width / 2, zone.y, zone.width / 2, zone.height, alpha)
        );
    }

    private void renderCapsule(Capsule caps, Rectangle zone) {
        caps.applyForEach(
          p -> spr.render(p.getSprite(spriteData), zone.x, zone.y, zone.width / 2, zone.height),
          p -> spr.render(p.getSprite(spriteData), zone.x + zone.width / 2, zone.y, zone.width / 2, zone.height)
        );
    }

    private void renderFallingCapsules() {
        model.controlledCapsules().forEach(c -> {
            c.preview().ifPresent(prev -> prev.applyToBoth(p -> renderCapsulePart(p, .5f)));
            c.applyToBoth(this::renderCapsulePart);
        });
    }

    private void renderUpcoming() {
        renderCapsule(model.upcoming().get(0), dimensions.nextCaps);
        if (model.capsuleCanBeHeld())
            model.getHeldCapsule().ifPresent(c -> renderCapsule(c, dimensions.holdCaps));
        else
            model.getHeldCapsule().ifPresent(c -> renderCapsule(c, dimensions.holdCaps, .6f));
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
          p -> spr.render(p.getSprite(), Dimensions.scaled(dimensions.tileAt(p.coordinates()), p.getScale()))
        );
        model.visualParticles().getManaParticles().forEach(p -> {
            Rectangle center = Dimensions.center(Dimensions.lerp(
              dimensions.tileAt(p.coordinates()),
              dimensions.sidekickZones.get(p.getTarget()).get(Dimensions.SidekickZone.Zone.HEAD), p.ratio()
            ));
            sr.drawCircle(center.x, center.y, 15, p.getTarget().color.value(.4f));
            sr.drawCircle(center.x, center.y, 5, p.getTarget().color.value());
        });
        model.visualParticles().getGenerationParticles().forEach(
          p -> spr.render(p.getCurrentSprite(), dimensions.nextBox)
        );
    }

    private void renderGameMessage() {
        model.gameEndManager().ifChecked(model, c -> tr.get(Font.BIG).drawText(c.getMessage(), dimensions.gridZone));
        if (model.isPaused()) tr.get(Font.BIG).drawText("PAUSED !", dimensions.gridZone);
    }

    void updateSprites() {
        spriteData.updateSprites();
    }

    public void render() {
        if (!model.isPaused()) shakeScreen();
        renderLayout();
        renderUpcoming();

        model.getSidekicks().forEach(this::renderSidekick);
        renderSidekickFocus();
        renderStack();
        renderFallingCapsules();

        renderParticles();
        renderGameMessage();
    }

    private void shakeScreen() {
        if (model.quakes().isEmpty()) return;
        camera.position.set(camOriginalPos);

        model.quakes().forEach(q -> camera.position.add(-2 + new Random().nextInt(4), -2 + new Random().nextInt(4), 0));
        camera.update();
    }

    public void dispose() {
        spr.dispose();
        sr.dispose();
        tr.values().forEach(TextRendererAdaptor::dispose);
    }
}
