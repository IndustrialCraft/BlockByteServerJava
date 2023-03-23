package com.github.industrialcraft.blockbyteserver.util;

public enum EHorizontalFace {
    FRONT(0),
    BACK(1),
    LEFT(2),
    RIGHT(3);
    public final byte id;
    EHorizontalFace(int id) {
        this.id = (byte) id;
    }
}
