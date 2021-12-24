package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Level {
    private final List<Gelule> droppingGelules = new ArrayList<>();
    private final List<Gelule> gelules = new ArrayList<>();
    private final List<Timer> timers = new ArrayList<>();
    private final Set<Color> colors;
    private final Grid grid;

    // TODO: replace by sidekick set
    public Level(Set<Color> colors) {
        colors.add(Color.randomBlank());
        this.colors = colors;
        grid = new Grid(6, 15);

        var dippingTimer = Timer.ofSeconds(1, this::dipOrAcceptGelule);
        var droppingTimer = Timer.ofMilliseconds(1, this::dipAllDroppingGelules);
        timers.addAll(Arrays.asList(dippingTimer, droppingTimer));

        spawnGelule();
    }

    public Set<Color> getColors() {
        return colors;
    }

    public Optional<Gelule> firstGelule() {
        return Optional.ofNullable(gelules.get(0));
    }

    public Grid getGrid() {
        return grid;
    }

    private void spawnGelule() {
        gelules.add(Gelule.randomNewInstance(this));
    }

    private void dipAllDroppingGelules() {
        // TODO: remove the ones who can't dip
        droppingGelules.forEach(g -> {
            if (g.bothVerify(c -> c.getPosition().y >= 1)) g.dip();
            else {
                droppingGelules.remove(g);
                accept(g);
            }
        });
    }

    private void accept(Gelule gelule) {
        gelule.forEachCapsule(grid::put);
    }

    private void performIfPossible(Consumer<Gelule> action, Predicate<Gelule> condition) {
        firstGelule().ifPresent(g -> {
            if (condition.test(g)) action.accept(g);
        });
    }

    public void dipOrAcceptGelule() {
        performIfPossible(Gelule::dip, g -> g.bothVerify(c -> c.getPosition().y >= 1));
    }

    public void flipGelule() {
        firstGelule().ifPresent(Gelule::flip);
    }

    public void moveGeluleLeft() {
        performIfPossible(Gelule::moveLeft, g -> g.bothVerify(c -> c.getPosition().x >= 1));
    }

    public void moveGeluleRight() {
        performIfPossible(Gelule::moveRight, g -> g.bothVerify(c -> c.getPosition().x < grid.getWidth() - 1));
    }

    public void dropGelule() {
        if (gelules.size() <= 0) return;
        Gelule gelule = gelules.remove(0);
        droppingGelules.add(gelule);
    }

    public void holdGelule() {
    }

    public void update() {
        timers.forEach(Timer::resetIfExceeds);
    }
}
