package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

class Corner {

    private Vector2 pos, gridPos;
    private Texture texture;
    private Circle circle;
    private final int pixmapSize = (int) GameScreen.tileWidth / 4;
    private Color color;
    private boolean visible;

    Corner(Vector2 gridPos, Color color) {
        this.gridPos = gridPos;
        this.color = color;
        this.pos = createPos(gridPos);
        this.visible = false;
        createTexture();

        circle = new Circle(pos.x + pixmapSize / 2, pos.y + pixmapSize / 2, pixmapSize / 10 * 4);
    }

    Corner (Vector2 gridPos, Color color, boolean visible) {
        this(gridPos, color);
        this.visible = visible;
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(pixmapSize, pixmapSize, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.fillCircle(pixmapSize / 2, pixmapSize / 2, pixmapSize / 10 * 4);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    private Vector2 createPos(Vector2 gridPos) {
        int posX = (int) (gridPos.x * GameScreen.TILE_MARGIN + gridPos.x * GameScreen.tileWidth + ((gridPos.y + 3) % 4 >= 2 ? GameScreen.tileWidth / 2 : 0) - pixmapSize / 2);
        int posY = (int) (gridPos.y * GameScreen.tileHeight / 4 + ((gridPos.y + 0) % 4 >= 2 ? GameScreen.tileHeight / 4 : 0) + Math.floor(gridPos.y / 4) * GameScreen.tileHeight / 2);
        posY -= pixmapSize / 2;
        posY += gridPos.y * GameScreen.TILE_MARGIN / 2;
        return new Vector2(posX, posY);
    }

    private boolean contains(float x, float y) {
        return circle.contains(x, y);
    }

    boolean contains(Vector3 mousePos) {
        return contains(mousePos.x, mousePos.y);
    }

    void draw(PolygonSpriteBatch pSB) {
        if (visible) {
            pSB.draw(texture, pos.x, pos.y);
        }
    }

    boolean isVisible() {
        return visible;
    }

    void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Color getColor() {
        return color;
    }

    void setColor(Color color) {
        this.color = color;
        createTexture();
    }

    Vector2 getGridPos() {
        return gridPos;
    }

    void setGridPos(Vector2 gridPos) {
        this.gridPos = gridPos;
        this.pos = createPos(gridPos);
    }

    void dispose() {
        texture.dispose();
    }

}
