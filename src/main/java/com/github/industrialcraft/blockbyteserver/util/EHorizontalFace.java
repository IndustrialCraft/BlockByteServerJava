package com.github.industrialcraft.blockbyteserver.util;

public enum EHorizontalFace {
    FRONT(0, EFace.Front),
    BACK(1, EFace.Back),
    LEFT(2, EFace.Left),
    RIGHT(3, EFace.Right);
    public final byte id;
    public final int xOffset;
    public final int zOffset;
    public final EFace fullFace;
    EHorizontalFace(int id, EFace fullFace) {
        this.id = (byte) id;
        this.xOffset = fullFace.xOffset;
        this.zOffset = fullFace.zOffset;
        this.fullFace = fullFace;
    }
    public EHorizontalFace opposite(){
        return switch (this){
            case BACK -> FRONT;
            case LEFT -> RIGHT;
            case FRONT -> BACK;
            case RIGHT -> LEFT;
        };
    }
    public static EHorizontalFace fromId(byte id){
        for(EHorizontalFace face : values()){
            if(face.id == id){
                return face;
            }
        }
        return null;
    }
}
