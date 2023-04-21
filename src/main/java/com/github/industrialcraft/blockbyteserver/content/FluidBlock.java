package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.InventoryContent;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class FluidBlock extends AbstractBlock {
    public final int clientId;
    public final Fluid fluid;
    public FluidBlock(AtomicInteger clientId, Fluid fluid) {
        this.clientId = clientId.getAndIncrement();
        this.fluid = fluid;
    }
    @Override
    public AbstractBlockInstance<FluidBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        return new FluidBlockInstance(this);
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        if(fluid.blockRenderData != null)
            renderData.put(clientId, fluid.blockRenderData);
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }

    @Override
    public Identifier getIdentifier() {
        return fluid.id;
    }

    @Override
    public boolean canPlace(PlayerEntity player, int x, int y, int z, World world) {
        return true;
    }
    @Override
    public boolean isNoCollide() {
        return false;
    }

    public static class FluidBlockInstance extends AbstractBlockInstance<FluidBlock> {
        public FluidBlockInstance(FluidBlock parent) {
            super(parent);
        }
        @Override
        public int getClientId() {
            return parent.clientId;
        }
        @Override
        public void onDestroy() {

        }
        @Override
        public boolean isValid() {
            return true;
        }
        @Override
        public void onSentToPlayer(PlayerEntity player) {}
        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {
        }
        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {

        }
        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return -1;
        }
        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            return null;
        }
    }
}
