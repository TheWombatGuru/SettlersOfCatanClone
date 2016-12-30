package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

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
    private static final int gridWidth = 4;
    private static final int gridHeight = 3;
    final static int TILE_MARGIN = 3;
    private final static float WIDTH_HEIGHT_RATIO = (float) Math.sqrt(3) / 2;
    final static float TILE_WIDTH = Math.min(
            (ShapesGame.WIDTH - (TILE_MARGIN * ((gridHeight > 1 ? gridWidth + .5f : gridWidth) - 1))) / (gridHeight > 1 ? gridWidth + .5f : gridWidth),
            (ShapesGame.HEIGHT - (TILE_MARGIN * (gridHeight - 1))) / (gridHeight * .75f + .25f) * WIDTH_HEIGHT_RATIO
            );
    final static float TILE_HEIGHT = TILE_WIDTH / WIDTH_HEIGHT_RATIO;
    private ArrayList<Player> players;
    private int playingPlayer = 0;

    GameScreen() {
        this.cam = new OrthographicCamera(800, 800);
        cam.setToOrtho(true); //If translating doesn't work anymore, add 800, 800 as 2nd and 3rd arguments
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        hoveredHexagon = new Hexagon(new Color(1, 1, 1, .3f), new Vector2(0, 0));
        hoveredCorner = new Corner(new Vector2(0, 0), new Color(0, 0, 0, .5f), true);
        hoveredRoad = new Road(new Vector2(3, 3), new Color(0, 0, 0, .5f), true);

        tiles = new ArrayList<Hexagon>();
        corners = new ArrayList<Corner>();
        roads = new ArrayList<Road>();

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                tiles.add(new Hexagon(TileType.getRandomTileType().getColor(), new Vector2(x, y)));
            }
        }

        createCircles();
        createRoads();

        pSB = new PolygonSpriteBatch();
        players = new ArrayList<Player>();
        players.add(new Player(Color.BLUE));
        players.add(new Player(Color.CORAL));
        players.add(new Player(Color.RED));
        players.add(new Player(Color.ORANGE));

        handleInput();

        List<String> addresses = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface ni : Collections.list(interfaces)){
                for(InetAddress address : Collections.list(ni.getInetAddresses()))
                {
                    if(address instanceof Inet4Address){
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Print the contents of our array to a string.  Yeah, should have used StringBuilder
        String ipAddress = "";
        for(String str:addresses)
        {
            ipAddress = ipAddress + str + "\n";
        }

        new Thread(new Runnable(){

            @Override
            public void run() {
                ServerSocketHints serverSocketHint = new ServerSocketHints();
                // 0 means no timeout.  Probably not the greatest idea in production!
                serverSocketHint.acceptTimeout = 0;

                // Create the socket server using TCP protocol and listening on 9021
                // Only one app can listen to a port at a time, keep in mind many ports are reserved
                // especially in the lower numbers ( like 21, 80, etc )
                ServerSocket serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, 9022, serverSocketHint);

                // Loop forever
                //noinspection InfiniteLoopStatement
                while(true){
                    // Create a socket
                    Socket socket = serverSocket.accept(null);

                    // Read data from the socket into a BufferedReader
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    try {
                        // Read to the next newline (\n) and display that text on labelMessage
                        System.out.println(buffer.readLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void createCircles() {
        for (int y = 0; y < gridHeight * 2 + 2; y++) {
            for (int x = 0; x < gridWidth + 1; x++) {
                if ((x == gridWidth && y == gridHeight * 2 + 1) || x == gridWidth && y == 0) {
                    continue;
                }

                if (y == gridHeight * 2 + 1 && gridHeight % 2 == 0) {
                    corners.add(new Corner(new Vector2(x + 1, y), new Color(1, 0, 0, 1)));
                } else {
                    corners.add(new Corner(new Vector2(x, y), new Color(1, 0, 0, 1)));
                }
            }
        }
    }

    private void createRoads() {
        for (int y = 0; y < gridHeight * 2 + 1; y++) {
            for (int x = 0; x < gridWidth * 2 + 1; x++) {
                if (y % 2 == 1 && x > gridWidth) {
                    continue;
                }

                if (y == 0 && x == gridWidth * 2) {
                    continue;
                }

                if (y == gridHeight * 2 && x == gridWidth * 2) {
                    continue;
                }

                if (gridHeight % 2 == 0 && y == gridHeight * 2) {
                    roads.add(new Road(new Vector2(x + 1, y), new Color(1, 0, 0, 1)));
                } else {
                    roads.add(new Road(new Vector2(x, y), new Color(1, 0, 0, 1)));
                }
            }
        }
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
                    case Input.Keys.A:
                        String textToSend;

                            textToSend = "Doesn't say much but likes clicking buttons\n";

                        SocketHints socketHints = new SocketHints();
                        // Socket will time our in 4 seconds
                        socketHints.connectTimeout = 4000;
                        //create the socket and connect to the server entered in the text box ( x.x.x.x format ) on port 9021
                        Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, "127.0.0.1", 9021, socketHints);
                        try {
                            // write our entered message to the stream
                            socket.getOutputStream().write(textToSend.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                return true;
            }

            public boolean touchDown (int x, int y, int pointer, int button) {
                switch (button) {
                    case Input.Buttons.LEFT:
                        for (Corner corner : corners) {
                            if (corner.contains(getMousePosInGame())) {
                                if (noVisibleNeighbours(corner) && !corner.isVisible()) {
                                    corner.setColor(players.get(playingPlayer).getColor());
                                    corner.setVisible(true);
                                }
                            }
                        }

                        for (Road road : roads) {
                            if (road.contains(getMousePosInGame()) && !road.isVisible()) {
                                road.setColor(players.get(playingPlayer).getColor());
                                road.setVisible(true);
                            }
                        }
                        System.out.println("TEST");
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

        hoveredHexagon.draw(pSB);

        for (Corner corner : corners) {
            corner.draw(pSB);
        }

        hoveredCorner.draw(pSB);

        for (Road road : roads) {
            road.draw(pSB);
        }

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

        if (!cornerHovered) {
            hoveredCorner.setGridPos(new Vector2(-2, -2));
        }

        if (!roadHovered) {
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

        if (!hovering) {
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