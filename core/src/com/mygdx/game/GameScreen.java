package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

class GameScreen implements Screen {

    private ShapesGame game;
    private PolygonSpriteBatch pSB;
    private ArrayList<Hexagon> tiles;
    private ArrayList<Road> roads;
    private ArrayList<Corner> corners;
    private OrthographicCamera cam;
    private Hexagon hoveredHexagon;
    private Corner hoveredCorner;
    private Road hoveredRoad;
    private static int gridWidth = 0;
    private static int gridHeight = 0;
//    private ArrayList<Player> players;
    final static int TILE_MARGIN = 3;
    private final static float WIDTH_HEIGHT_RATIO = (float) Math.sqrt(3) / 2;
    static float tileWidth;
    static float tileHeight;
    private int playingPlayer = 0;
    private Socket socket;
    private boolean gameStarted = false;

    GameScreen(Socket socket) {
        this.socket = socket;
        this.cam = new OrthographicCamera(800, 800);
        cam.setToOrtho(true);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        hoveredHexagon = null;
        hoveredCorner = null;
        hoveredRoad = null;

        tiles = new ArrayList<>();
        corners = new ArrayList<>();
        roads = new ArrayList<>();

        pSB = new PolygonSpriteBatch();

        handleInput();
        handleSocketEvents();
    }

    public void createBoard(JSONObject map) throws JSONException {
        Iterator<?> yKeysCount = map.keys();

        int largestWidth = 0;

        for (int i = 0; yKeysCount.hasNext(); ++i) {
            gridHeight++;
            String yCoord = (String) yKeysCount.next();
            if (map.get(yCoord) instanceof JSONObject) {
                JSONObject jsonRow = (JSONObject) map.get(yCoord);
                Iterator<?> xKeysCount = jsonRow.keys();

                int thisWidth = 0;

                for (int j = 0; xKeysCount.hasNext(); ++j) {
                    thisWidth++;
                    xKeysCount.next();
                }

                largestWidth = Math.max(largestWidth, thisWidth);
            }

        }

        gridWidth = largestWidth;

        tileWidth = Math.min(
                (ShapesGame.WIDTH - (TILE_MARGIN * ((gridHeight > 1 ? gridWidth + .5f : gridWidth) - 1))) / (gridHeight > 1 ? gridWidth + 1f : gridWidth),
                (ShapesGame.HEIGHT - (TILE_MARGIN * (gridHeight - 1))) / (gridHeight * .75f + .25f) * WIDTH_HEIGHT_RATIO
        );

        tileHeight = tileWidth / WIDTH_HEIGHT_RATIO;

        Iterator<?> yKeys = map.keys();

        while (yKeys.hasNext()) {
            String yCoord = (String) yKeys.next();
            int y = Integer.parseInt(yCoord);
            if (map.get(yCoord) instanceof JSONObject) {
                JSONObject jsonRow = (JSONObject) map.get(yCoord);
                Iterator<?> xKeys = jsonRow.keys();

                while(xKeys.hasNext()) {
                    String xCoord = (String) xKeys.next();
                    int x = Integer.parseInt(xCoord);
                    JSONObject cell = (JSONObject) jsonRow.get(xCoord);
                    int tileType = (int) cell.get("tileType");
                    int tileNumber = (int) cell.get("tileNumber");

                    tiles.add(new Hexagon(TileType.getVALUES().get(tileType), new Vector2(x, y), tileNumber));

                    createCircles(x, y);
                    createRoads(x, y);
                }
            }
        }

        hoveredHexagon = new Hexagon(new Color(1, 1, 1, .3f), new Vector2(0, 0), 0);
        hoveredCorner = new Corner(new Vector2(0, 0), new Color(0, 0, 0, .5f), true);
        hoveredRoad = new Road(new Vector2(3, 3), new Color(0, 0, 0, .5f), true);

    }

    private void createCircles(int x, int y) {
        boolean c1 = false, c2 = false, c3 = false, c4 = false, c5 = false, c6 = false;
        if (y %2 == 0) {
            for (Corner corner : corners)
                if (corner.getGridPos().equals(new Vector2(x, y * 2)))
                    c1 = true;

            for (Corner corner : corners)
                if (corner.getGridPos().equals(new Vector2(x, y * 2 + 3)))
                    c2 = true;

            if (!c1)
                corners.add(new Corner(new Vector2(x, y * 2), new Color(1, 0, 0, 1)));

            if (!c2)
                corners.add(new Corner(new Vector2(x, y * 2 + 3), new Color(1, 0, 0, 1)));
        } else {
            for (Corner corner : corners)
                if (corner.getGridPos().equals(new Vector2(x + 1, y * 2)))
                    c1 = true;

            for (Corner corner : corners)
                if (corner.getGridPos().equals(new Vector2(x + 1, y * 2 + 3)))
                    c2 = true;

            if (!c1)
                corners.add(new Corner(new Vector2(x + 1, y * 2), new Color(1, 0, 0, 1)));

            if (!c2)
                corners.add(new Corner(new Vector2(x + 1, y * 2 + 3), new Color(1, 0, 0, 1)));
        }

        for (Corner corner : corners)
            if (corner.getGridPos().equals(new Vector2(x, y * 2 + 1)))
                c3 = true;

        for (Corner corner : corners)
            if (corner.getGridPos().equals(new Vector2(x, y * 2 + 2)))
                c4 = true;

        for (Corner corner : corners)
            if (corner.getGridPos().equals(new Vector2(x + 1, y * 2 + 1)))
                c5 = true;

        for (Corner corner : corners)
            if (corner.getGridPos().equals(new Vector2(x + 1, y * 2 + 2)))
                c6 = true;

        if (!c3)
            corners.add(new Corner(new Vector2(x, y * 2 + 1), new Color(1, 0, 0, 1)));

        if (!c4)
            corners.add(new Corner(new Vector2(x, y * 2 + 2), new Color(1, 0, 0, 1)));

        if (!c5)
            corners.add(new Corner(new Vector2(x + 1, y * 2 + 1), new Color(1, 0, 0, 1)));

        if (!c6)
            corners.add(new Corner(new Vector2(x + 1, y * 2 + 2), new Color(1, 0, 0, 1)));
    }

    private void createRoads(int x, int  y) {
        boolean c1 = false, c2 = false, c3 = false, c4 = false, c5 = false, c6 = false;
        if (y %2 == 0) {
            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2, y * 2)))
                    c1 = true;

            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2 + 1, y * 2)))
                    c2 = true;

            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2, y * 2 + 2)))
                    c3 = true;

            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2 + 1, y * 2 + 2)))
                    c4 = true;

            if (!c1)
                roads.add(new Road(new Vector2(x * 2, y * 2), new Color(1, 0, 0, 1)));

            if (!c2)
                roads.add(new Road(new Vector2(x * 2 + 1, y * 2), new Color(1, 0, 0, 1)));

            if (!c3)
                roads.add(new Road(new Vector2(x * 2, y * 2 + 2), new Color(1, 0, 0, 1)));

            if (!c4)
                roads.add(new Road(new Vector2(x * 2 + 1, y * 2 + 2), new Color(1, 0, 0, 1)));
        } else {
            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2 + 1, y * 2)))
                    c1 = true;

            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2 + 2, y * 2)))
                    c2 = true;

            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2 + 1, y * 2 + 2)))
                    c3 = true;

            for (Road road : roads)
                if (road.getGridPos().equals(new Vector2(x * 2 + 2, y * 2 + 2)))
                    c4 = true;

            if (!c1)
                roads.add(new Road(new Vector2(x * 2 + 1, y * 2), new Color(1, 0, 0, 1)));

            if (!c2)
                roads.add(new Road(new Vector2(x * 2 + 2, y * 2), new Color(1, 0, 0, 1)));

            if (!c3)
                roads.add(new Road(new Vector2(x * 2 + 1, y * 2 + 2), new Color(1, 0, 0, 1)));

            if (!c4)
                roads.add(new Road(new Vector2(x * 2 + 2, y * 2 + 2), new Color(1, 0, 0, 1)));
        }

        for (Road road : roads)
            if (road.getGridPos().equals(new Vector2(x, y * 2 + 1)))
                c5 = true;

        for (Road road : roads)
            if (road.getGridPos().equals(new Vector2(x + 1, y * 2 + 1)))
                c6 = true;

        if (!c5)
            roads.add(new Road(new Vector2(x, y * 2 + 1), new Color(1, 0, 0, 1)));

        if (!c6)
            roads.add(new Road(new Vector2(x + 1, y * 2 + 1), new Color(1, 0, 0, 1)));
    }

    private void handleSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            Gdx.app.log("SocketIO", "Connected");
        }).on("socketID", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String id = data.getString("id");
                Gdx.app.log("SocketIO", "My ID: " + id);
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error retrieving ID");
            }
        }).on("newPlayer", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String id = data.getString("id");
                Gdx.app.log("SocketIO", "New player connected; ID: " + id);
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error retrieving new player's ID");
            }
        }).on("playerList", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                JSONArray playerList = data.getJSONArray("playerList");
                Gdx.app.log("SocketIO", "Player list: " + playerList.toString());
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error retrieving player list");
            }
        }).on("playerLeft", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String id = data.getString("id");
                Gdx.app.log("SocketIO", "A player disconnected; ID: " + id);
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error retrieving disconnected player's id");
            }
        }).on("gameInfo", message -> {
            gameStarted = true;
            Gdx.app.log("SocketIO", message[0].toString());
        });
    }

    private void handleInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            public boolean keyDown (int keycode) {
                switch (keycode) {
                    case Input.Keys.N:
                        if (playingPlayer == 3) {
                            playingPlayer = 0;
                        } else {
                            playingPlayer++;
                        }
                        break;
                    case Input.Keys.D:
                        break;
                }
                return true;
            }

            public boolean touchDown (int x, int y, int pointer, int button) {
                switch (button) {
                    case Input.Buttons.LEFT:
                        for (Corner corner : corners) {
                            if (corner.contains(getMousePosInGame())) {
                                if (noVisibleNeighbours(corner) && !corner.isVisible()) {
                                    socket.emit("placeVillageRequest", x, y);
                                    socket.on("placeVillage", args -> {
                                        if ((Integer) args[0] == x && (Integer) args[1] == y) {
                                            JSONObject players = (JSONObject) args[2];
                                            try {
                                                JSONObject currentPlayer = (JSONObject) players.get((String) args[3]);
                                                corner.setColor(new Color((Integer) currentPlayer.get("color")));
                                                corner.setVisible(true);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        for (Road road : roads) {
                            if (road.contains(getMousePosInGame()) && !road.isVisible()) {
                                road.setColor(Color.BLUE);
                                road.setVisible(true);
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    private boolean noVisibleNeighbours(Corner corner) {
        float xPos = corner.getGridPos().x;
        float yPos = corner.getGridPos().y;

        if (getCornerAt(xPos, yPos + 1).isVisible() || getCornerAt(xPos, yPos - 1).isVisible()) {
            return false;
        }

        if (yPos % 4 == 0) {
            return !getCornerAt(xPos + 1, yPos + 1).isVisible();
        } else if (yPos % 4 == 1) {
            return !getCornerAt(xPos - 1, yPos - 1).isVisible();
        } else if (yPos % 4 == 2) {
            return !getCornerAt(xPos - 1, yPos + 1).isVisible();
        } else if (yPos % 4 == 3) {
            return !getCornerAt(xPos + 1, yPos - 1).isVisible();
        }

        return true;
    }

    private Corner getCornerAt(float xPos, float yPos) {
        for (Corner corner : corners) {
            if (corner.getGridPos().equals(new Vector2(xPos, yPos))) {
                return corner;
            }
        }

        return new Corner(new Vector2(0, 0), new Color(0, 0, 0, 0));
    }

    @Override
    public void render(float delta) {
        boolean hovering = false;
        boolean cornerHovered = false;
        boolean roadHovered = false;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));
        Gdx.gl.glClearColor(1, 1, 1, 1);

        cam.update();
        pSB.setProjectionMatrix(cam.combined);

        handleInput();


        pSB.begin();
        for (Hexagon hexagon : tiles) {
            hexagon.draw(pSB);
        }

        if (hoveredHexagon != null)
            hoveredHexagon.draw(pSB);

        for (Corner corner : corners) {
            corner.draw(pSB);
        }
        if (hoveredCorner != null)
            hoveredCorner.draw(pSB);

        for (Road road : roads) {
            road.draw(pSB);
        }

        if (hoveredRoad != null)
            hoveredRoad.draw(pSB);

        pSB.end();

        for (Corner corner : corners) {
            if (corner.contains(getMousePosInGame())) {
                hoveredCorner.setGridPos(corner.getGridPos());
                cornerHovered = true;
            }
        }

        for (Road road : roads) {
            if (road.contains(getMousePosInGame())) {
                hoveredRoad.setGridPos(road.getGridPos());
                roadHovered = true;
            }
        }

        if (!cornerHovered && hoveredCorner != null) {
            hoveredCorner.setGridPos(new Vector2(-2, -2));
        }

        if (!roadHovered && hoveredRoad != null) {
            hoveredRoad.setGridPos(new Vector2(-2, -2));
        }

        for (Hexagon hexagon : tiles) {
            if (hexagon.contains(getMousePosInGame())) {
                for (Corner corner : corners) {
                    if (corner.contains(getMousePosInGame())) {
                        cornerHovered = true;
                    }
                }

                if (!cornerHovered && !roadHovered) {
                    hovering = true;
                    hoveredHexagon.setGridPos(hexagon.getGridPos());
                }
            }
        }

        if (!hovering && hoveredHexagon != null) {
            hoveredHexagon.setGridPos(new Vector2(-2, -2));
        }
    }

    private Vector3 getMousePosInGame() {
        return cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    }

    //TODO: Vergroten werkt niet

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        pSB.dispose();
        for (Corner corner : corners) {
            corner.dispose();
        }
        for (Hexagon hexagon : tiles) {
            hexagon.dispose();
        }
        hoveredCorner.dispose();
        hoveredHexagon.dispose();
    }

}