package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;

public class Block {
    public static Block AIR = new Block(null, 0, null);

    public final BlockRegistry.BlockRenderData renderData;
    public final int clientId;
    public final LootTable lootTable;
    private final BlockInstance instance;
    public Block(BlockRegistry.BlockRenderData renderData, int clientId, LootTable lootTable) {
        this.renderData = renderData;
        this.clientId = clientId;
        this.lootTable = lootTable;
        this.instance = new BlockInstance(this);
    }
    public int getClientId() {
        return clientId;
    }
    public BlockInstance createBlockInstance(Chunk chunk, int x, int y, int z){
        return instance;
    }
    public boolean onRightClick(World world, BlockPosition blockPosition, BlockInstance instance, PlayerEntity player){
        return false;
    }
}
