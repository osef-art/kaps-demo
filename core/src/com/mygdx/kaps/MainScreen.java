package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.level.Color;
import com.mygdx.kaps.level.GameView;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.sound.SoundStream;

import java.util.Arrays;
import java.util.HashSet;

public class MainScreen extends ApplicationAdapter {
    public static OrthographicCamera camera;
    public static InputHandler inputHandler;
    public static SoundStream soundStream;

    private Level game;
    private GameView view;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        camera.translate(0, Gdx.graphics.getHeight());

        game = new Level(new HashSet<>(Arrays.asList(Color.COLOR_1, Color.COLOR_2)));
        view = new GameView(game);
        inputHandler = new InputHandler(game);

        Gdx.input.setInputProcessor(inputHandler);
        soundStream = new SoundStream();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.update();
        view.render();
    }

    @Override
    public void dispose() {
        view.dispose();
    }
}
