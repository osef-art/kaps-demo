package com.mygdx.kaps.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygdx.kaps.MainScreen;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("KAPS");
        config.setWindowIcon(Files.FileType.Local, "android/assets/sprites/icons/icon.png");

        config.setWindowedMode(480, 800);
        config.setResizable(false);
        config.useVsync(true);

        new Lwjgl3Application(new MainScreen(args), config);
    }
}
