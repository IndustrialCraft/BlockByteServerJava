package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;

import java.util.HashMap;

public abstract class AbstractBlock {
    public abstract AbstractBlockInstance createBlockInstance(Chunk chunk, int x, int y, int z, Object data);
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player){
        return false;
    }
    public abstract LootTable getLootTable();
    public abstract void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData);
    public abstract int getDefaultClientId();
}
