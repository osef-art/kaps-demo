package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.game.GameScene;
import com.mygdx.kaps.game.GameView;
import com.mygdx.kaps.sound.SoundStream;

public class MainScreen extends ApplicationAdapter {
    public static OrthographicCamera camera;
    public static InputHandler inputHandler;
    public static SoundStream soundStream;

    private GameScene game;
    private GameView view;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        camera.translate(0, Gdx.graphics.getHeight());

        inputHandler = new InputHandler();
        Gdx.input.setInputProcessor(inputHandler);

        soundStream = new SoundStream();

        game = new GameScene();
        view = new GameView(game);
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
