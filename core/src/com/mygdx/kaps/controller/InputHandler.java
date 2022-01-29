package com.mygdx.kaps.controller;

import com.badlogic.gdx.InputProcessor;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.time.PeriodicTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InputHandler implements InputProcessor {
    private enum Key {
        LEFT_KEY(Level::moveCapsuleLeft, 100.0, 21, 45), // Q, LEFT ARR.
        RIGHT_KEY(Level::moveCapsuleRight, 100.0, 22, 32), // D, RIGHT ARR.
        DOWN_KEY(Level::dipOrFreezeCapsule, 100.0, 20, 47), // S, DOWN ARR.
        FLIP_KEY(Level::flipCapsule, 150.0, 19, 54), // Z, UP ARR.
        DROP_KEY(Level::dropCapsule, 62), // SPACEBAR
        HOLD_KEY(Level::holdCapsule, 31, 50, 30), // V

        PREVIEW_KEY(l -> l.parameters().togglePreview(), 52), // X
        PAUSE_KEY(l -> l.parameters().togglePause(), 44), // P

        ESCAPE_KEY(l -> System.exit(0), 29, 131, 68), // A, ESC, !
        ;

        private final Set<Integer> codes;
        private final Consumer<Level> effect;
        private final double refreshRate;

        Key(Consumer<Level> consumer, int... codes) {
            this(consumer, 0, codes);
        }

        Key(Consumer<Level> consumer, double hold, int... codes) {
            Objects.requireNonNull(consumer);
            this.codes = Arrays.stream(codes).boxed().collect(Collectors.toUnmodifiableSet());
            effect = consumer;
            refreshRate = hold;
        }

        private static Optional<Key> ofCode(int code) {
            return Arrays.stream(values()).filter(k -> k.codes.contains(code)).findFirst();
        }

        private boolean canBeHold() {
            return refreshRate > 0;
        }
    }

    private final Map<Key, PeriodicTask> pressedKeys = new HashMap<>();
    private final Level model;

    public InputHandler(Level lvl) {
        Objects.requireNonNull(lvl);
        model = lvl;
    }

    public void update() {
        pressedKeys.values().forEach(PeriodicTask::resetIfExceeds);
    }

    // input detection
    @Override
    public boolean keyDown(int keycode) {
        // debug
//        System.out.println(keycode);
        Key.ofCode(keycode).ifPresent(key -> {
            key.effect.accept(model);
            if (key.canBeHold())
                pressedKeys.put(key, PeriodicTask.everyMilliseconds(key.refreshRate, () -> key.effect.accept(model)));
        });
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Key.ofCode(keycode).ifPresent(pressedKeys::remove);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
