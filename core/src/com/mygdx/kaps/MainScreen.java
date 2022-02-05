package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.level.LevelBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainScreen extends ApplicationAdapter {
    private final LinkedList<Level> levels = new LinkedList<>();
    private final String args;
    private InputHandler inputs;

    public MainScreen(String... args) {
        this.args = String.join(" ", args);
    }

    private LevelBuilder loadedLevelSequence() {
        LevelBuilder lvlBuilder = new LevelBuilder();

        if (args.isBlank()) IntStream.rangeClosed(0, 20).forEach(lvlBuilder::addLevel);
        else Arrays.stream(args.split("-"))
          .filter(Predicate.not(""::equals))
          .forEach(cmd -> {
              var flag = cmd.charAt(0);
              var args = Arrays.stream(cmd.split(" "))
                .skip(1)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());

              switch (flag) {
                  case 'l':
                      if (args.isEmpty()) lvlBuilder.addRandomLevel();
                      else args.forEach(lvl -> {
                          if (lvl.equals("?")) lvlBuilder.addRandomGrid();
                          else lvlBuilder.addLevel(Integer.parseInt(lvl));
                      });
                      break;
                  case 's':
                      args.forEach(lvlBuilder::addSidekick);
              }
          });

        return lvlBuilder;
    }

    private Level currentLevel() {
        return levels.get(0);
    }

    private void bindInputsToCurrentLevel() {
        Gdx.input.setInputProcessor(inputs = new InputHandler(currentLevel()));
    }

    @Override
    public void create() {
        levels.addAll(loadedLevelSequence().buildSequence());
        bindInputsToCurrentLevel();
    }

    @Override
    public void render() {
        inputs.update();
        currentLevel().render();

        if (currentLevel().isOver()) {
            levels.removeFirst();
            if (levels.isEmpty()) System.exit(0);
            bindInputsToCurrentLevel();
        }
    }

    @Override
    public void dispose() {
        levels.forEach(Level::dispose);
    }
}
