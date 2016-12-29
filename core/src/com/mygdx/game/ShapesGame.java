package com.mygdx.game;

import com.badlogic.gdx.Game;


public class ShapesGame extends Game {
	public static final int WIDTH = 1600;
	public static final int HEIGHT = 900;
	public static final String TITLE = "Shape tests";
	private GameScreen gameScreen;

	@Override
	public void create () {
		gameScreen = new GameScreen();
		setScreen(gameScreen);
	}

	@Override
	public void resize(int width, int height) {}
	
	@Override
	public void dispose () {
	}
}
