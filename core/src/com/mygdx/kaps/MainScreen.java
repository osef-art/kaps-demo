package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.level.LevelBuilder;

public class MainScreen extends ApplicationAdapter {
    private final String[] args;
    private InputHandler inputs;
    private Level game;

    public MainScreen(String... args) {
        this.args = args;
    }

    private Level loadedLevel() {
        LevelBuilder lvlBuilder = new LevelBuilder();
        int flags = 0;

        while (args.length > flags) {
            switch (args[flags]) {
                case "-s":
                    if (args.length > flags + 1)
                        lvlBuilder.addSidekick(args[flags + 1]);
                    break;
                case "-l":
                    lvlBuilder.setRandomLevel();
                    if (args.length > flags + 1 && args[flags + 1].charAt(0) != '-')
                        lvlBuilder.setLevel(Integer.parseInt(args[flags + 1]));
                    break;
            }
            flags ++;
        }
        return lvlBuilder.build();
    }

    @Override
    public void create() {
        game = loadedLevel();
        Gdx.input.setInputProcessor(inputs = new InputHandler(game));
    }

    @Override
    public void render() {
        inputs.update();
        game.render();
    }

    @Override
    public void dispose() {
        game.dispose();
    }
}
