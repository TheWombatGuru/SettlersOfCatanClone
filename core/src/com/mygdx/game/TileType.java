package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum TileType {

    WOOL (Color.GREEN),
    WOOD (Color.FOREST),
    WEED (Color.YELLOW),
    ORE (Color.GRAY),
    STONE (Color.ORANGE);

    private final Color color;
    private static final List<TileType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    TileType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public static TileType getRandomTileType() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
