package com.mygdx.kaps.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Level {
    private final List<Gelule> gelules = new ArrayList<>();
    private final Set<Color> colors;
    private final Grid grid;

    // TODO: replace by sidekick set
    public Level(Set<Color> colors) {
        colors.add(Color.randomBlank());
        this.colors = colors;
        grid = new Grid(6, 15);

        spawnGelule();
    }

    private void spawnGelule() {
        gelules.add(Gelule.randomNewInstance(this));
    }

    public Set<Color> getColors() {
        return colors;
    }

    public Optional<Gelule> getGelule() {
        return Optional.ofNullable(gelules.get(0));
    }

    public Grid getGrid() {
        return grid;
    }

    public void update() {
    }
}
