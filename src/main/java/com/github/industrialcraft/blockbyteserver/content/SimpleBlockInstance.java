package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.BlockBreakingCalculator;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.util.List;

public class SimpleBlockInstance<T extends SimpleBlock> extends AbstractBlockInstance<T>{
    public SimpleBlockInstance(T parent) {
        super(parent);
    }
    @Override
    public int getClientId() {
        return parent.getDefaultClientId();
    }
    @Override
    public void onDestroy(){

    }
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void onSentToPlayer(PlayerEntity player) {}
    @Override
    public void onNeighborUpdate(BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}

    @Override
    public void postSet(Chunk chunk, int x, int y, int z) {

    }
    @Override
    public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
        return BlockBreakingCalculator.calculateBlockBreakingTime(item, parent.tool, parent.minToolLevel, parent.blockHardness);
    }

    @Override
    public List<ItemStack> getLoot(PlayerEntity player) {
        LootTable lootTable = BlockBreakingCalculator.calculateWhetherBlockDrops(player.getItemInHand(), parent.tool, parent.minToolLevel, parent.blockHardness)?parent.lootTable:null;
        return lootTable==null?null:lootTable.toItems(player.getChunk().parent.itemRegistry);
    }
}
