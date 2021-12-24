package com.mygdx.kaps.game;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Grid {
    private static class Column {
        private final List<Tile> tiles;

        private Column(int tiles) {
            this.tiles = new ArrayList<>(tiles);
            IntStream.range(0, tiles).forEach(c -> this.tiles.add(new Tile()));
        }

        public int size() {
            return tiles.size();
        }
    }
    private final List<Column> columns;

    public Grid(int columns, int rows) {
        this.columns = new ArrayList<>(rows);
        IntStream.range(0, columns).forEach(c -> this.columns.add(new Column(rows)));
    }

    public Vector2 getDimensions() {
        return new Vector2(columns.size(), columns.get(0).size());
    }
}
