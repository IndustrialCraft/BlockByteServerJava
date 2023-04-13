package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.util.List;

public abstract class AbstractBlockInstance<T extends AbstractBlock> {
    public final T parent;
    public AbstractBlockInstance(T parent) {
        this.parent = parent;
    }
    public abstract int getClientId();
    public abstract void onDestroy();
    public abstract boolean isValid();
    public abstract void onSentToPlayer(PlayerEntity player);
    public abstract void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face);
    public abstract void postSet(Chunk chunk, int x, int y, int z);
    protected void updateToClients(Chunk chunk, int xWorld, int yWorld, int zWorld){
        chunk.announceToViewersExcept(new MessageS2C.SetBlock(xWorld, yWorld, zWorld, getClientId()), null);
    }
    public abstract float getBlockBreakingTime(ItemStack item, PlayerEntity player);
    public abstract List<ItemStack> getLoot(PlayerEntity player);
    public void onBreak(PlayerEntity player, int x, int y, int z){}
}
