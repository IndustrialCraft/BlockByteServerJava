package com.github.industrialcraft.blockbyteserver.world;

public class Block {
    public static Block AIR = new Block(0);
    public static Block GRASS = new Block(1);
    public static Block COBBLE = new Block(2);

    public final int clientId;
    public Block(int clientId) {
        this.clientId = clientId;
    }
    public int getClientId() {
        return clientId;
    }
}
