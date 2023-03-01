package com.github.industrialcraft.blockbyteserver.util;

public record Position(float x, float y, float z) {
    public BlockPosition toBlockPos(){
        return new BlockPosition((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }
}
