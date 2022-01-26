package com.mygdx.kaps.level.gridobject;

import com.mygdx.kaps.level.Gauge;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.sound.SoundStream;
import com.mygdx.kaps.time.TaskManager;

public abstract class CooldownGerm extends Germ {
    private final TaskManager tasks = new TaskManager();
    private final Gauge cooldown;

    public CooldownGerm(Color color, GermKind kind) {
        super(color, kind);
        this.cooldown = Gauge.full(kind.getCooldown());
    }

    @Override
    public boolean hasCooldown() {
        return true;
    }

    int turnsLeft() {
        return cooldown.getValue();
    }

    public double gaugeRatio() {
        return cooldown.ratio();
    }

    public boolean isReady() {
        return cooldown.isEmpty();
    }

    public SoundStream.SoundStore attackSound() {
        return kind.getAttackType().sound();
    }

    public void reset() {
        cooldown.fill();
    }

    public void decreaseCooldown() {
        cooldown.decreaseIfPossible();
    }

    public void trigger(Level level) {
        tasks.add(kind.newAttack(level, this).periodicMoves());
        reset();
    }

    public void updateTasks() {
        tasks.update();
    }
}

final class VirusGerm extends CooldownGerm {
    VirusGerm(Color color) {
        super(color, GermKind.VIRUS);
    }
}

final class ThornGerm extends CooldownGerm {
    ThornGerm(Color color) {
        super(color, GermKind.THORN);
    }
}