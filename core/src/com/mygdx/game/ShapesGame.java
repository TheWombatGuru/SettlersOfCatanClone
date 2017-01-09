package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class ShapesGame extends Game {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 800;
	public static final String TITLE = "Shape tests";
	private GameScreen gameScreen;
	private Socket socket;
	private static final String SERVER_IP = "92.110.62.170";
	private static final int SERVER_PORT = 9021;

	@Override
	public void create () {
		connectSocket();
		gameScreen = new GameScreen(socket);
		setScreen(gameScreen);
	}

	private void connectSocket() {
		try {
			socket = IO.socket("http://" + SERVER_IP + ":" + SERVER_PORT);
			socket.connect();

			socket.on("mapData", args -> {
				JSONObject data = (JSONObject) args[0];
				JSONObject mapData = null;
				try {
					mapData = (JSONObject) data.get("mapData");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				JSONObject finalMapData = mapData;
				new Thread(() -> {
					Gdx.app.postRunnable(() -> {
						try {
							gameScreen.createBoard(finalMapData);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					});
				}).start();
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void resize(int width, int height) {}
	
	@Override
	public void dispose () {
	}
}
