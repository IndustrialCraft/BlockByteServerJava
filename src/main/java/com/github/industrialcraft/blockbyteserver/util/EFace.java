package com.github.industrialcraft.blockbyteserver.util;

public enum EFace {
    Front(0, 0, 0, -1),
    Back(1, 0, 0, 1),
    Up(2, 0, 1, 0),
    Down(3, 0, -1, 0),
    Left(4, -1, 0, 0),
    Right(5, 1, 0, 0);
    public final byte id;
    public final int xOffset;
    public final int yOffset;
    public final int zOffset;
    EFace(int id, int xOffset, int yOffset, int zOffset) {
        this.id = (byte) id;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }
    public static EFace fromId(byte id){
        for (EFace face : values()) {
            if(face.id == id)
                return face;
        }
        return null;
    }
}
