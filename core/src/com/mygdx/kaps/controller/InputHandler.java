package com.mygdx.kaps.controller;

import com.badlogic.gdx.InputProcessor;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.time.Timer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InputHandler implements InputProcessor {
    public enum Key {
        LEFT_KEY(Level::moveGeluleLeft, 75.0, 21, 45), // Q, LEFT ARR.
        RIGHT_KEY(Level::moveGeluleRight, 75.0, 22, 32), // D, RIGHT ARR.
        DOWN_KEY(Level::dipOrAcceptGelule, 75.0, 20, 47), // S, DOWN ARR.
        FLIP_KEY(Level::flipGelule, 100.0, 19, 54), // Z, UP ARR.
        DROP_KEY(Level::dropGelule, 62), // C, L
        HOLD_KEY(Level::holdGelule, 31, 50, 30), // V, M

        ESCAPE_KEY(m -> System.exit(0), 29, 131, 68), // A, ESC, !
        ;

        private final Set<Integer> codes;
        private final Consumer<Level> effect;
        private final double refreshRate;

        Key(Consumer<Level> consumer, int... codes) {
            this(consumer, 0, codes);
        }

        Key(Consumer<Level> consumer, double hold, int... codes) {
            this.codes = Arrays.stream(codes).boxed().collect(Collectors.toUnmodifiableSet());
            effect = consumer;
            refreshRate = hold;
        }

        @Override
        public String toString() {
            return "[" + super.toString().split("_")[0] + "]";
        }

        private static Optional<Key> ofCode(int code) {
            return Arrays.stream(values()).filter(k -> k.codes.contains(code)).findFirst();
        }

        private boolean canBeHold() {
            return refreshRate > 0;
        }
    }

    private final HashMap<Key, Timer> pressedKeys = new HashMap<>();
    private final Level model;

    public InputHandler(Level model) {
        this.model = model;
    }

    public void update() {
        pressedKeys.values().forEach(Timer::resetIfExceeds);
    }

    // input detection
    @Override
    public boolean keyDown(int keycode) {
        // debug
//        System.out.println(keycode);
        Key.ofCode(keycode).ifPresent(key -> {
            key.effect.accept(model);
            if (key.canBeHold())
                pressedKeys.put(key, Timer.ofMilliseconds(key.refreshRate, () -> key.effect.accept(model)));
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
