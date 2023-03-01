package com.github.industrialcraft.blockbyteserver.util;

public record BlockPosition(int x, int y, int z) {
    public ChunkPosition toChunkPos(){
        return new ChunkPosition((int) Math.floor(x/16f), (int) Math.floor(y/16f), (int) Math.floor(z/16f));
    }
    public int getChunkXOffset(){
        return ((x%16)+16)%16;
    }
    public int getChunkYOffset(){
        return ((y%16)+16)%16;
    }
    public int getChunkZOffset(){
        return ((z%16)+16)%16;
    }
}
