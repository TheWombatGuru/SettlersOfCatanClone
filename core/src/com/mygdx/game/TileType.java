package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TileType {

    WOOL (new Texture(Gdx.files.internal("wool.png")), 0),
    WOOD (new Texture(Gdx.files.internal("wood.png")), 1),
    WHEAT (new Texture(Gdx.files.internal("wheat.png")), 2),
    ORE (new Texture(Gdx.files.internal("ore.png")), 3),
    STONE (new Texture(Gdx.files.internal("stone.png")), 4),
    DESERT (new Texture(Gdx.files.internal("desert.png")), 5),
    WATER (new Texture(Gdx.files.internal("water.png")), 6);

    private final Texture texture;
    private final int type;
    private static final List<TileType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    TileType(Texture texture, int type) {
        this.texture = texture;
        this.type = type;
    }

    public Texture getTexture() {
        return texture;
    }

    public static List<TileType> getVALUES() {
        return VALUES;
    }
}
