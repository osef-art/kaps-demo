package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.level.GameView;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.sound.SoundStream;

import java.util.Random;

public class MainScreen extends ApplicationAdapter {
    public static OrthographicCamera camera;
    private InputHandler inputs;
    public static SoundStream soundStream;

    private Level game;
    private GameView view;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        camera.translate(0, Gdx.graphics.getHeight());

        game = new Random().nextBoolean() ?
                 Level.loadFrom("android/assets/levels/level0") :
                 Level.randomLevel(6, 15, 10);
        view = new GameView(game);
        inputs = new InputHandler(game);

        Gdx.input.setInputProcessor(inputs);
        soundStream = new SoundStream();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        inputs.update();
        game.update();
        view.render();
    }

    @Override
    public void dispose() {
        view.dispose();
    }
}
