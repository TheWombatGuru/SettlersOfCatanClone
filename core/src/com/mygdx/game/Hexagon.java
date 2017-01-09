package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

class Hexagon {

    private Color color;
    private PolygonSprite pSprite;
    private Vector2 gridPos;
    private Texture hexagonTexture;
    private TileType tileType;
    private int tileNumber;
    private Texture numberTexture;
    private int pixmapSize = (int) GameScreen.tileWidth / 2;
    private FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Georgia.ttf"));
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    private BitmapFont font;

    Hexagon(TileType tileType, Vector2 gridPos, int tileNumber) {
        this.tileType = tileType;
        this.gridPos = gridPos;
        this.hexagonTexture = createHexagonTexture();
        this.pSprite = createNewPolygon(gridPos);
        this.tileNumber = tileNumber;
        createNumberTexture();
        createFont();

    }

    Hexagon(Color color, Vector2 gridPos, int tileNumber) {
        this.gridPos = gridPos;
        this.hexagonTexture = createHexagonTexture(color);
        this.pSprite = createNewPolygon(gridPos);
        this.tileNumber = tileNumber;
        createNumberTexture();
    }

    private void createFont() {
        parameter.size = pixmapSize / 10 * 4;
        parameter.flip = true;
        font = generator.generateFont(parameter);
    }

    private void createNumberTexture() {
        Pixmap pixmap = new Pixmap(pixmapSize, pixmapSize, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, .5f));
        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.fillCircle(pixmapSize / 2, pixmapSize / 2, pixmapSize / 10 * 4);
        numberTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private Texture createHexagonTexture() {
        return tileType.getTexture();
    }

    private Texture createHexagonTexture(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.fill();
        hexagonTexture = new Texture(pix);
        pix.dispose();
        return hexagonTexture;
    }

    private PolygonSprite createNewPolygon(Vector2 gridPos) {
        float tileSize = GameScreen.tileHeight;

        float texHeight = hexagonTexture.getHeight();
        float texWidth = texHeight * ((float) Math.sqrt(3) / 2);

        Vector2 start = getGridCoords(gridPos);

        float[] vertices = {
                texWidth / 2, 0,
                texWidth, texHeight * .25f,
                texWidth, texHeight * .75f,
                texWidth / 2, texHeight,
                0, texHeight * .75f,
                0, texHeight * .25f};

        TextureRegion textureRegion = new TextureRegion(hexagonTexture);
        textureRegion.flip(false, true);
        PolygonRegion polyRegion = new PolygonRegion(textureRegion, vertices, new EarClippingTriangulator().computeTriangles(vertices).toArray());
        PolygonSprite pSprite = new PolygonSprite(polyRegion);
        pSprite.setBounds(start.x, start.y, tileSize, tileSize);
        pSprite.setOrigin(start.x + 100, start.y + 100);
        return pSprite;
    }

    Vector2 getGridCoords(Vector2 gridPos) {
        float tileWidth = GameScreen.tileWidth;
        float tileHeight = GameScreen.tileHeight;

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

        if (tileNumber != 0) {
            pSB.draw(numberTexture, pSprite.getX() + GameScreen.tileWidth / 2 - pixmapSize / 2, pSprite.getY() + GameScreen.tileHeight / 2 - pixmapSize / 2);
            font.setColor(Color.WHITE);
            font.draw(pSB, Integer.toString(tileNumber), pSprite.getX() + GameScreen.tileWidth / 2 - pixmapSize / 2, pSprite.getY() + GameScreen.tileHeight / 2 - font.getLineHeight() / 2, (float) pixmapSize, Align.center, false);
        }
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
        hexagonTexture.dispose();
        numberTexture.dispose();
    }
}
