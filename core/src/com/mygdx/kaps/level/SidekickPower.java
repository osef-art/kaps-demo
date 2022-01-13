package com.mygdx.kaps.level;

import com.mygdx.kaps.Utils;
import com.mygdx.kaps.level.gridobject.Coordinates;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class SidekickPower {
    public static BiConsumer<Sidekick.SidekickId, Grid> paint5RandomObjects() {
        return (id, grid) -> Utils.getRandomSetOf(grid.stack(), 5).forEach(o -> grid.repaint(o, id.color()));
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hit3RandomObjects() {
        return (id, grid) -> Utils.getRandomSetOf(grid.stack(), 3).forEach(o -> grid.hit(o, id.damage()));
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hit1RandomObjectAndAdjacents() {
        return (id, grid) -> Utils.getOptionalRandomFrom(grid.stack()).ifPresent(o -> {
            grid.hit(o, id.damage());
            Arrays.asList(new Coordinates(0, 1), new Coordinates(0, -1), new Coordinates(1, 0), new Coordinates(-1, 0))
              .forEach(c -> grid.hit(c.addedTo(o.coordinates())));
        });
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hit2RandomGerms() {
        return (id, grid) -> Utils.getRandomSetOf(grid.germStack(), 2).forEach(g -> grid.hit(g, id.damage()));
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hit1RandomGerm() {
        return (id, grid) -> Utils.getOptionalRandomFrom(grid.germStack()).ifPresent(g -> grid.hit(g, id.damage()));
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hitRandomLine() {
        return (id, grid) -> Utils.getOptionalRandomFrom(grid.stack()).ifPresent(
          picked -> grid.stack().stream()
            .filter(o -> o.coordinates().y == picked.coordinates().y)
            .forEach(o -> grid.hit(o, id.damage()))
        );
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hitRandomColumn() {
        return (id, grid) -> Utils.getOptionalRandomFrom(grid.stack()).ifPresent(
          picked -> grid.stack().stream()
            .filter(o -> o.coordinates().x == picked.coordinates().x)
            .forEach(o -> grid.hit(o, id.damage()))
        );
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> hitRandomDiagonals() {
        return (id, grid) -> Utils.getOptionalRandomFrom(grid.stack()).ifPresent(
          picked -> grid.stack().stream()
            .filter(o -> Math.abs(o.coordinates().x - picked.coordinates().x) ==
                           Math.abs(o.coordinates().y - picked.coordinates().y)
            )
            .forEach(o -> grid.hit(o, id.damage()))
        );
    }

    public static BiConsumer<Sidekick.SidekickId, Grid> doNothing() {
        return (id, grid) -> {
        };
    }
}
