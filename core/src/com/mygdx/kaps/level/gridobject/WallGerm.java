package com.mygdx.kaps.level.gridobject;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.kaps.level.Gauge;
import com.mygdx.kaps.renderer.AnimatedSprite;
import com.mygdx.kaps.renderer.SpriteData;

import java.util.function.Consumer;

public final class WallGerm extends Germ {
    private static final int maxHealth = 4;
    private final Gauge health;

    WallGerm(Color color, int health) {
        super(color, GermKind.WALL, 3, 40);
        this.health = new Gauge(health, maxHealth);
        if (health <= 0 || maxHealth < health)
            throw new IllegalArgumentException(String.format("Invalid health: %d / %d", health, maxHealth));
    }

    WallGerm(Color color) {
        this(color, maxHealth);
    }

    @Override
    public int getScore() {
        return isDestroyed() ? super.getScore() : 10;
    }

    public Gauge getHealth() {
        return health;
    }

    @Override
    public Sprite getSprite(SpriteData data) {
        return data.getWallGerm(health.getValue(), color()).getCurrentSprite();
    }

    @Override
    public AnimatedSprite poppingAnim() {
        return SpriteData.poppingWallAnimation(health.getValue(), color());
    }


    public boolean isDestroyed() {
        return health.isEmpty();
    }

    public void takeHit() {
        health.decreaseIfPossible();
    }

    public void ifWall(Consumer<WallGerm> action) {
        action.accept(this);
    }
}
