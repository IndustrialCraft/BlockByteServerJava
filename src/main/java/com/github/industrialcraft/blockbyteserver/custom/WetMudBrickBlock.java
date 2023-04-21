package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WetMudBrickBlock extends AbstractBlock {
    public static final int DRY_TIME = 20*60*1;
    public final int clientId;
    public final BlockRegistry.BlockRenderData[] renderData;
    public WetMudBrickBlock(AtomicInteger clientId) {
        this.renderData = new BlockRegistry.BlockRenderData[8];
        for(int i = 0;i < 8;i++){
            JsonObject jsonRenderData = new JsonObject();
            jsonRenderData.addProperty("type", "static");
            jsonRenderData.addProperty("texture", (i<4?"wet":"dry")+"_mud_brick");
            jsonRenderData.addProperty("model", "wet_mud_brick_" + (i%4));
            jsonRenderData.addProperty("no_collide", true);
            renderData[i] = new BlockRegistry.BlockRenderData(jsonRenderData);
        }
        this.clientId = clientId.get();
        clientId.addAndGet(8);
    }
    @Override
    public AbstractBlockInstance<WetMudBrickBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        int bricks = 1;
        int dryCounter = DRY_TIME;
        if(data instanceof DataInputStream stream){
            try {
                bricks = stream.readByte();
                dryCounter = stream.readInt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new WetMudBrickBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), chunk, bricks, dryCounter);
    }
    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        WetMudBrickBlockInstance brickBlockInstance = (WetMudBrickBlockInstance) instance;
        if(brickBlockInstance.bricks >= 4)
            return false;
        ItemStack stack = player.getItemInHand();
        if(stack != null){
            if(((BlockByteItem)stack.getItem()).id.equals(Identifier.of("bb","wet_mud_brick"))){
                brickBlockInstance.bricks++;
                stack.removeCount(1);
                player.updateHand();
                brickBlockInstance.resyncToClients();
                return true;
            }
        }
        return false;
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        for(int i = 0;i < 8;i++)
            renderData.put(clientId+i, this.renderData[i]);
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }
    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb","wet_mud_brick");
    }
    @Override
    public boolean isSerializable() {
        return true;
    }
    @Override
    public boolean canPlace(PlayerEntity player, int x, int y, int z, World world) {
        return BlockHelper.needSupportCanPlace(world, x, y, z);
    }
    @Override
    public boolean isNoCollide() {
        return true;
    }
    public static class WetMudBrickBlockInstance extends AbstractBlockInstance<WetMudBrickBlock> implements ISerializable, ITicking{
        public final int x;
        public final int y;
        public final int z;
        public final Chunk chunk;
        private int bricks;
        private int dryCounter;
        public WetMudBrickBlockInstance(WetMudBrickBlock parent, int x, int y, int z, Chunk chunk, int bricks, int dryCounter) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.chunk = chunk;
            this.bricks = bricks;
            this.dryCounter = dryCounter;
        }
        @Override
        public int getClientId() {
            return parent.clientId+(bricks-1)+(dryCounter<=0?4:0);
        }
        @Override
        public void onDestroy() {}
        @Override
        public boolean isValid() {
            return true;
        }
        @Override
        public void onSentToPlayer(PlayerEntity player) {}

        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {
            BlockHelper.needsSupportNeighborUpdate(world, position, newInstance, face);
        }
        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {}
        @Override
        public void serialize(DataOutputStream stream) throws IOException {
            stream.writeByte(bricks);
            stream.writeInt(dryCounter);
        }
        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return 0.5f;
        }

        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            //todo
            return null;
        }
        @Override
        public void tick() {
            if(dryCounter > 0) {
                dryCounter--;
                if(dryCounter == 0){
                    resyncToClients();
                }
            }
        }
        private void resyncToClients(){
            updateToClients(chunk, x, y, z);
        }
    }
}
