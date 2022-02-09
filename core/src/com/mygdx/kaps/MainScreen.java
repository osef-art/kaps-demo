package com.mygdx.kaps;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.mygdx.kaps.controller.InputHandler;
import com.mygdx.kaps.level.Level;
import com.mygdx.kaps.level.LevelLoader;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainScreen extends ApplicationAdapter {
    private Level level;
    private final LevelLoader lvlLoader = new LevelLoader();
    private final String args;
    private InputHandler inputs;

    public MainScreen(String... args) {
        this.args = String.join(" ", args);
    }

    private void loadLevelSequence() {
        if (args.isBlank()) IntStream.rangeClosed(0, 20).forEach(lvlLoader::addLevel);
        else Arrays.stream(args.split("-"))
          .filter(Predicate.not(String::isBlank))
          .forEach(cmd -> {
              var flag = cmd.charAt(0);
              var args = Arrays.stream(cmd.split(" "))
                .skip(1)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());

              switch (flag) {
                  case 'l':
                      if (args.isEmpty()) lvlLoader.addRandomLevel();
                      else args.forEach(lvl -> {
                          switch (lvl) {
                              case "!":
                                  lvlLoader.addRandomGrid();
                                  break;
                              case "?":
                                  lvlLoader.addRandomLevel();
                                  break;
                              default:
                                  if (!lvl.chars().mapToObj(c -> (char) c).allMatch(Character::isDigit))
                                      throw new IllegalArgumentException(String.format("Was expecting a number, found '%s'", lvl));
                                  lvlLoader.addLevel(Integer.parseInt(lvl));
                          }
                      });
                      break;
                  case 's':
                      args.forEach(lvlLoader::addSidekick);
              }
          });

    }

    private void loadNextLevel() {
        lvlLoader.takeNextLevel().ifPresentOrElse(lvl -> {
            level = lvl;
            Gdx.input.setInputProcessor(inputs = new InputHandler(level));
        }, ()->System.exit(0));
    }

    @Override
    public void create() {
        loadLevelSequence();
        loadNextLevel();
    }

    @Override
    public void render() {
        inputs.update();
        level.render();

        if (level.isOver()) {
            level.dispose();
            loadNextLevel();
        }
    }

    @Override
    public void dispose() {
        level.dispose();
    }
}
