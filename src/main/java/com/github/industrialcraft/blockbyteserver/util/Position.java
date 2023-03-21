package com.github.industrialcraft.blockbyteserver.util;

public record Position(float x, float y, float z) {
    public BlockPosition toBlockPos(){
        return new BlockPosition((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }
    public Position add(float x, float y, float z){
        return new Position(this.x+x, this.y+y, this.z+z);
    }
}
