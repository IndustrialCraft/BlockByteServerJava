package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.ItemScatterer;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LogPileBlock extends AbstractBlock {
    public final int clientId;
    public final BlockRegistry.BlockRenderData[] renderData;
    public LogPileBlock(AtomicInteger clientId) {
        this.renderData = new BlockRegistry.BlockRenderData[4];
        for(int i = 0;i < renderData.length;i++){
            JsonObject jsonRenderData = new JsonObject();
            jsonRenderData.addProperty("type", "static");
            jsonRenderData.addProperty("texture", "log_pile");
            jsonRenderData.addProperty("model", "log_pile_" + i);
            renderData[i] = new BlockRegistry.BlockRenderData(jsonRenderData);
        }
        this.clientId = clientId.get();
        clientId.addAndGet(4);
    }
    @Override
    public AbstractBlockInstance<LogPileBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        return new LogPileBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), chunk);
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        for (int i = 0;i < this.renderData.length;i++) {
            renderData.put(clientId+i, this.renderData[i]);
        }
    }

    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        return ((LogPileBlockInstance)instance).onRightClick(player);
    }

    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb","conveyor");
    }

    @Override
    public boolean canPlace(PlayerEntity player, int x, int y, int z, World world) {
        return true;
    }
    @Override
    public boolean isNoCollide() {
        return false;
    }
    public static class LogPileBlockInstance extends AbstractBlockInstance<LogPileBlock> {
        public final int x;
        public final int y;
        public final int z;
        private boolean isValid;
        public final Chunk chunk;
        private int logs;
        public LogPileBlockInstance(LogPileBlock parent, int x, int y, int z, Chunk chunk) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.chunk = chunk;
            this.isValid = true;
            this.logs = 0;
        }
        @Override
        public int getClientId() {
            return parent.clientId+logs;
        }
        @Override
        public void onDestroy() {
            ArrayList<ItemStack> items = new ArrayList<>();
            for(int i = 0;i < logs;i++)
                items.add(new ItemStack(chunk.parent.itemRegistry.getItem(Identifier.of("bb","log")), 1));
            ItemScatterer.scatter(items, chunk.parent, new Position(x, y, z), 0.5f);
            this.isValid = false;
        }
        @Override
        public boolean isValid() {
            return isValid;
        }
        @Override
        public void onSentToPlayer(PlayerEntity player) {
        }
        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}
        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {}
        public boolean onRightClick(PlayerEntity player){
            ItemStack hand = player.getItemInHand();
            if(hand == null){
                player.inventory.addItem(new ItemStack(player.getChunk().parent.itemRegistry.getItem(Identifier.of("bb", "log")), 1));
                logs--;
                if(logs == 0){
                    chunk.parent.setBlock(new BlockPosition(x, y, z), SimpleBlock.AIR, null);
                } else {
                    updateToClients(chunk, x, y, z);
                }
            } else if(logs < 3 && ((BlockByteItem)hand.getItem()).id.equals(Identifier.of("bb","log"))){
                hand.removeCount(1);
                player.updateHand();
                logs++;
                updateToClients(chunk, x, y, z);
            } else {
                return false;
            }
            return true;
        }
        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return BlockBreakingCalculator.calculateBlockBreakingTime(item, ETool.AXE, 0, 2);
        }
        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            return null;
        }
    }
}
