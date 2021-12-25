package com.mygdx.kaps.level;


import com.mygdx.kaps.time.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Level {
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

    public List<Gelule> getGelules() {
        return gelules;
    }

    public Grid getGrid() {
        return grid;
    }

    private Stream<Gelule> floatingGelulesStream() {
        return gelules.stream().filter(Predicate.not(Gelule::isFalling));
    }

    private void dipAllDroppingGelules() {
        gelules.stream()
          .filter(Gelule::isFalling)
          .forEach(g -> {
              if (g.bothVerify(c -> c.getCoordinates().y >= 1)) g.dip();
              else accept(g);
          });
    }

    private void performIfPossibleElse(Predicate<Gelule> condition, Consumer<Gelule> action,
                                       Consumer<Gelule> alternative) {
        floatingGelulesStream().forEach(g -> {
            if (condition.test(g)) action.accept(g);
            else alternative.accept(g);
        });
    }

    private void performIfPossible(Predicate<Gelule> condition, Consumer<Gelule> action) {
        performIfPossibleElse(condition, action, g -> {});
    }

    // moves
    public void dipOrAcceptGelule() {
        performIfPossibleElse(g -> g.bothVerify(c -> c.getCoordinates().y >= 1), Gelule::dip, this::accept);
    }

    public void flipGelule() {
        floatingGelulesStream().forEach(Gelule::flip);
    }

    public void moveGeluleLeft() {
        performIfPossible(g -> g.bothVerify(c -> c.getCoordinates().x >= 1), Gelule::moveLeft);
    }

    public void moveGeluleRight() {
        performIfPossible(g -> g.bothVerify(c -> c.getCoordinates().x < grid.getWidth() - 1), Gelule::moveRight);
    }

    public void dropGelule() {
        floatingGelulesStream().forEach(Gelule::startFalling);
    }

    public void holdGelule() {
    }

    // update
    private void spawnGelule() {
        gelules.add(Gelule.randomNewInstance(this));
    }

    private void accept(Gelule gelule) {
        gelule.forEachCapsule(grid::put);
        gelule.freeze();
    }

    public void update() {
        gelules.removeIf(Gelule::isFrozen);
        if (gelules.stream().noneMatch(Predicate.not(Gelule::isFalling))) spawnGelule();
        timers.forEach(Timer::resetIfExceeds);
    }
}
