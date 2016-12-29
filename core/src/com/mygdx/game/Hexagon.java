package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

class Hexagon {

    private Color color;
    private PolygonSprite pSprite;
    private Vector2 gridPos;
    private Texture texture;

    Hexagon(Color color, Vector2 gridPos) {
        this.color = color;
        this.gridPos = gridPos;
        this.pSprite = createNewPolygon(color, gridPos);
    }

    private PolygonSprite createNewPolygon(Color color, Vector2 gridPos) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.fill();
        texture = new Texture(pix);
        pix.dispose();

        float tileWidth = GameScreen.TILE_WIDTH;
        float tileHeight = GameScreen.TILE_HEIGHT;

        Vector2 start = getGridCoords(gridPos);

        float[] vertices = {
                start.x + tileWidth / 2, start.y,
                start.x + tileWidth, start.y + tileHeight * .25f,
                start.x + tileWidth, start.y + tileHeight * .75f,
                start.x + tileWidth / 2, start.y + tileHeight,
                start.x, start.y + tileHeight * .75f,
                start.x, start.y + tileHeight * .25f};

        PolygonRegion polyRegion = new PolygonRegion(new TextureRegion(texture), vertices, new EarClippingTriangulator().computeTriangles(vertices).toArray());
        PolygonSprite pSprite = new PolygonSprite(polyRegion);
        pSprite.setOrigin(start.x + 100, start.y + 100);
        return pSprite;
    }

    Vector2 getGridCoords(Vector2 gridPos) {
        float tileWidth = GameScreen.TILE_WIDTH;
        float tileHeight = GameScreen.TILE_HEIGHT;

        float xStart = gridPos.x * tileWidth + (gridPos.y % 2 == 1 ? tileWidth / 2 : 0) + gridPos.x * GameScreen.TILE_MARGIN + (gridPos.y % 2 == 1 ? GameScreen.TILE_MARGIN / 2 : 0);
        float yStart = gridPos.y * (tileHeight * .75f) + gridPos.y * GameScreen.TILE_MARGIN;

        return new Vector2(xStart, yStart);
    }

    void setGridPos(Vector2 gridPos) {
        this.gridPos = gridPos;

        Vector2 gridCoords = getGridCoords(gridPos);
        pSprite.setPosition(gridCoords.x, gridCoords.y);
    }

    private float[] getTransformedVertices() {
        float[] transformedVertices = new float[12];
        for (int i = 0; i < 6; i++) {
            transformedVertices[i * 2] = pSprite.getVertices()[i * 5];
            transformedVertices[i * 2 + 1] = pSprite.getVertices()[i * 5 + 1];
        }
        return transformedVertices;
    }

    void draw(PolygonSpriteBatch pSB) {
        this.pSprite.draw(pSB);
    }

    boolean contains(float x, float y) {
        float[] ver = this.getTransformedVertices();
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new com.badlogic.gdx.graphics.Color(color));
        pix.fill();
        pSprite.getRegion().getRegion().setTexture(new Texture(pix));
    }

    PolygonSprite getpSprite() {
        return pSprite;
    }

    public void setpSprite(PolygonSprite pSprite) {
        this.pSprite = pSprite;
    }

    public Vector2 getGridPos() {
        return gridPos;
    }

    void dispose() {
        texture.dispose();
    }
}
