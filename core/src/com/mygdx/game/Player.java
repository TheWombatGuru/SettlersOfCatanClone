package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import io.socket.client.Socket;

public class Player {

    private Socket socket;
    private Color color = Color.BLUE;

    public Player(Socket socket) {
        this.socket = socket;
    }

    public Color getColor() {
        return color;
    }

    public Socket getSocket() {
        return socket;
    }
}
