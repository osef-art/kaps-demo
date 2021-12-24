package com.mygdx.kaps.controller;

import com.badlogic.gdx.InputProcessor;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InputHandler implements InputProcessor {
    public enum Key {
        LEFT_KEY(21, 45), // Q, LEFT ARR.
        RIGHT_KEY(22, 32), // D, RIGHT ARR.
        FLIP_KEY(19, 54), // Z, UP ARR.
        DOWN_KEY(20, 47), // S, DOWN ARR.
        DROP_KEY(31, 40), // C, L
        HOLD_KEY(50, 41), // V, M

        ESCAPE_KEY(29, 131, 68), // A, ESC, !
        ;

        private final Set<Integer> codes;

        Key(int... codes) {
            this.codes = Arrays.stream(codes).boxed().collect(Collectors.toUnmodifiableSet());
        }

        public static Optional<Key> ofCode(int code) {
            return Arrays.stream(values()).filter(k -> k.codes.contains(code)).findFirst();
        }

        @Override
        public String toString() {
            return "[" + super.toString().split("_")[0] + "]";
        }
    }

    // input detection
    @Override
    public boolean keyDown(int keycode) {
//        System.out.println(keycode); //debug
        Key.ofCode(keycode).ifPresent(key -> {
            if (key == Key.ESCAPE_KEY) System.exit(0);
        });
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
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
