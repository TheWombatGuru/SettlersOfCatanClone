package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Road {

    private Vector2 pos, gridPos;
    private Color color;
    private Texture texture;
    private Polygon rectangle;
    private boolean visible;
    private float width = GameScreen.tileHeight / 8 * 2, height = width / 6;
    private float rotation;

    public Road(Vector2 gridPos, Color color) {
        this.gridPos = gridPos;
        this.rotation = createRot(gridPos);
        this.color = color;
        this.pos = createPos(gridPos);
        this.rectangle = new Polygon(new float[]{
                pos.x, pos.y,
                pos.x + width, pos.y,
                pos.x + width, pos.y + height,
                pos.x, pos.y + height
            });
        rectangle.setOrigin(pos.x + width / 2, pos.y + height / 2);
        rectangle.rotate(rotation);
        createTexture();

        this.visible = false;
    }

    public Road (Vector2 gridPos, Color color, boolean visible) {
        this(gridPos, color);
        this.visible = visible;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        createTexture();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Polygon getRectangle() {
        return rectangle;
    }

    public void setRectangle(Polygon rectangle) {
        this.rectangle = rectangle;
    }

    public Vector2 getGridPos() {
        return gridPos;
    }

    void setGridPos(Vector2 gridPos) {
        this.gridPos = gridPos;
        this.pos = createPos(gridPos);
        this.rotation = createRot(gridPos);
    }

    private boolean contains(float x, float y) {
        float[] ver = rectangle.getTransformedVertices();
        final int numFloats = ver.length;
        int intersects = 0;

        for (int i = 0; i < numFloats; i += 2) {
            float x1 = ver[i];
            float y1 = ver[i + 1];
            float x2 = ver[(i + 2) % numFloats];
            float y2 = ver[(i + 3) % numFloats];
            if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) && x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1)) intersects++;
        }
        return (intersects & 1) == 1;
    }

    boolean contains (Vector3 pos) {
        return contains(pos.x, pos.y);
    }

    private float createRot(Vector2 gridPos) {
        if (gridPos.y % 2 == 0) {
            if (gridPos.x % 2 == 1) {
                if (gridPos.y % 4 > 1) {
                    return 150;
                } else {
                    return 30;
                }
            } else {
                if (gridPos.y % 4 > 1) {
                    return 30;
                } else {
                    return 150;
                }
            }
        } else {
            return 90;
        }
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.fillRectangle(0, 0, (int) width, (int) height);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    private Vector2 createPos(Vector2 gridPos) {
        float yPos = GameScreen.tileHeight / 8 - height / 2 + gridPos.y / 2 * GameScreen.tileHeight / 4 * 3 + gridPos.y / 2 * GameScreen.TILE_MARGIN;
        float xPos;
        if (gridPos.y % 2 == 0) {
            xPos = GameScreen.tileWidth / 4 - width / 2 + (GameScreen.tileWidth + GameScreen.TILE_MARGIN) * (gridPos.x / 2);
        } else {
            xPos = -width / 2 + (GameScreen.tileWidth + GameScreen.TILE_MARGIN) * gridPos.x;
        }

        if (gridPos.y % 4 == 3) {
            xPos += GameScreen.tileWidth / 2;
        }

        return new Vector2(xPos, yPos);
    }

    void draw(PolygonSpriteBatch pSB) {
        if (visible) {
            pSB.draw(texture, pos.x, pos.y, width / 2, height / 2, (int) width, (int) height, 1, 1, rotation, 1, 1, (int) width, (int) height, false, false);
        }
    }
}
