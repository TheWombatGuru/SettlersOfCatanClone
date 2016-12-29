package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.ShapesGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = ShapesGame.WIDTH;
		config.height = ShapesGame.HEIGHT;
		config.title = ShapesGame.TITLE;
		config.samples = 3;
		new LwjglApplication(new ShapesGame(), config);
	}
}
