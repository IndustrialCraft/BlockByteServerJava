package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;

import java.util.HashMap;

public abstract class AbstractBlock {
    public abstract AbstractBlockInstance createBlockInstance(Chunk chunk, int x, int y, int z, Object data);
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player){
        return false;
    }
    public abstract void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData);
    public abstract int getDefaultClientId();
    public abstract Identifier getIdentifier();
    public boolean isSerializable(){return false;}
    public void postInit(BlockRegistry blockRegistry){}
    public abstract boolean canPlace(PlayerEntity player, int x, int y, int z, World world);
    public abstract boolean isNoCollide();
}
