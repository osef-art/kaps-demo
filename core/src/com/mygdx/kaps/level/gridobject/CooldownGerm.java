package com.mygdx.kaps.level.gridobject;

import com.mygdx.kaps.level.Gauge;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.TaskManager;

import java.util.function.Consumer;

public abstract class CooldownGerm extends Germ {
    private final TaskManager tasks = new TaskManager();
    private final Gauge cooldown;
    private boolean attacking;

    public CooldownGerm(Color color, GermKind kind, int mana, int score) {
        super(color, kind, mana, score);
        this.cooldown = Gauge.full(kind.getCooldown());
    }

    @Override
    public boolean hasCooldown() {
        return true;
    }

    public int turnsLeft() {
        return cooldown.getValue();
    }

    public double gaugeRatio() {
        return cooldown.ratio();
    }

    public boolean isReady() {
        return cooldown.isEmpty();
    }

    public boolean isAttacking() {
        return attacking;
    }

    public SoundStream.SoundStore attackSound() {
        return kind.getAttackType().sound();
    }

    public void startAttacking() {
        attacking = true;
    }

    public void stopAttacking() {
        attacking = false;
    }

    public void decreaseCooldown() {
        cooldown.decreaseIfPossible();
    }

    public void trigger(Level level) {
        tasks.add(kind.newAttack(level, this).periodicMoves());
        cooldown.fill();
    }

    public void updateTasks() {
        tasks.update();
    }

    public void ifHasCooldownElse(Consumer<CooldownGerm> cooldownAction, Consumer<Germ> germAction) {
        cooldownAction.accept(this);
    }
}

final class VirusGerm extends CooldownGerm {
    VirusGerm(Color color) {
        super(color, GermKind.VIRUS, 3, 30);
    }
}

final class ThornGerm extends CooldownGerm {
    ThornGerm(Color color) {
        super(color, GermKind.THORN, 3, 35);
    }
}