package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.level.LevelBuilder;

import java.util.LinkedList;

public class MainScreen extends ApplicationAdapter {
    private final LinkedList<Level> levels = new LinkedList<>();
    private final String[] args;
    private InputHandler inputs;

    public MainScreen(String... args) {
        this.args = args;
    }

    private void loadLevelSequence() {
        LevelBuilder lvlBuilder = new LevelBuilder();
        int flags = 0;

        while (args.length > flags) {
            switch (args[flags]) {
                case "-s":
                    if (args.length > flags + 1)
                        lvlBuilder.addSidekick(args[flags + 1]);
                    break;
                case "-l":
                    lvlBuilder.addRandomLevel();
                    if (args.length > flags + 1 && args[flags + 1].charAt(0) != '-')
                        lvlBuilder.addLevel(Integer.parseInt(args[flags + 1]));
                    break;
            }
            flags++;
        }
        levels.addAll(lvlBuilder.buildSequence());
    }

    @Override
    public void create() {
        loadLevelSequence();
        Gdx.input.setInputProcessor(inputs = new InputHandler(levels.get(0)));
    }

    @Override
    public void render() {
        inputs.update();
        levels.get(0).render();

        if (levels.get(0).isOver()) {
            levels.removeFirst();
            if (levels.isEmpty()) System.exit(0);
            inputs = new InputHandler(levels.get(0));
        }
    }

    @Override
    public void dispose() {
        levels.forEach(Level::dispose);
    }
}
