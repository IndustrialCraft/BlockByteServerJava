package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlock;
import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.BlockRegistry;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LogBlock extends AbstractBlock {
    public final int clientId;
    public final BlockRegistry.BlockRenderData[] blockRenderData;
    public final LogBlockInstance[] states;
    public LogBlock(AtomicInteger clientId) {
        this.blockRenderData = new BlockRegistry.BlockRenderData[8*7];
        this.states = new LogBlockInstance[8*7];
        for(int i = 0;i < 8;i++) {
            for (EFace face : EFace.allWithNull()) {
                this.states[(i*7)+(face==null?0:(face.id+1))] = new LogBlockInstance(this, (byte) i, face);
            }
        }
        this.clientId = clientId.get();
        clientId.addAndGet(8);
    }

    @Override
    public void postInit(BlockRegistry blockRegistry) {
        for(int i = 0;i < 7;i++){
            JsonObject renderData = new JsonObject();
            renderData.addProperty("type", "static");
            renderData.addProperty("texture", "log");
            renderData.addProperty("model", "log_"+((i+1)*2));
            {
                JsonObject connections = new JsonObject();
                {
                    JsonObject down = new JsonObject();
                    for (int j = 0; j < 8; j++) {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_d" + Math.min(i, j));
                        connection.addProperty("texture", "log");
                        down.add("" + (clientId + j), connection);
                    }
                    {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_d" + Math.min(i, 1));
                        connection.addProperty("texture", "log");
                        down.add("" + (blockRegistry.getBlock(Identifier.of("bb","leave")).getDefaultClientId()), connection);
                    }
                    connections.add("down", down);
                }
                {
                    JsonObject up = new JsonObject();
                    for (int j = 0; j < 8; j++) {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_u" + Math.min(i, j));
                        connection.addProperty("texture", "log");
                        up.add("" + (clientId + j), connection);
                    }
                    {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_u" + Math.min(i, 1));
                        connection.addProperty("texture", "log");
                        up.add("" + (blockRegistry.getBlock(Identifier.of("bb","leave")).getDefaultClientId()), connection);
                    }
                    connections.add("up", up);
                }
                {
                    JsonObject left = new JsonObject();
                    for (int j = 0; j < 8; j++) {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_l" + Math.min(i, j));
                        connection.addProperty("texture", "log");
                        left.add("" + (clientId + j), connection);
                    }
                    {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_l" + Math.min(i, 1));
                        connection.addProperty("texture", "log");
                        left.add("" + (blockRegistry.getBlock(Identifier.of("bb","leave")).getDefaultClientId()), connection);
                    }
                    connections.add("left", left);
                }
                {
                    JsonObject right = new JsonObject();
                    for (int j = 0; j < 8; j++) {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_r" + Math.min(i, j));
                        connection.addProperty("texture", "log");
                        right.add("" + (clientId + j), connection);
                    }
                    {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_r" + Math.min(i, 1));
                        connection.addProperty("texture", "log");
                        right.add("" + (blockRegistry.getBlock(Identifier.of("bb","leave")).getDefaultClientId()), connection);
                    }
                    connections.add("right", right);
                }
                {
                    JsonObject front = new JsonObject();
                    for (int j = 0; j < 8; j++) {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_f" + Math.min(i, j));
                        connection.addProperty("texture", "log");
                        front.add("" + (clientId + j), connection);
                    }
                    {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_f" + Math.min(i, 1));
                        connection.addProperty("texture", "log");
                        front.add("" + (blockRegistry.getBlock(Identifier.of("bb","leave")).getDefaultClientId()), connection);
                    }
                    connections.add("front", front);
                }
                {
                    JsonObject back = new JsonObject();
                    for (int j = 0; j < 8; j++) {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_b" + Math.min(i, j));
                        connection.addProperty("texture", "log");
                        back.add("" + (clientId + j), connection);
                    }
                    {
                        JsonObject connection = new JsonObject();
                        connection.addProperty("model", "log_b" + Math.min(i, 1));
                        connection.addProperty("texture", "log");
                        back.add("" + (blockRegistry.getBlock(Identifier.of("bb","leave")).getDefaultClientId()), connection);
                    }
                    connections.add("back", back);
                }
                renderData.add("connections", connections);
            }
            blockRenderData[i] = new BlockRegistry.BlockRenderData(renderData);
        }
        {
            JsonObject renderData = new JsonObject();
            renderData.addProperty("type", "cube");
            renderData.addProperty("north", "log");
            renderData.addProperty("south", "log");
            renderData.addProperty("left", "log");
            renderData.addProperty("right", "log");
            renderData.addProperty("up", "log");
            renderData.addProperty("down", "log");
            blockRenderData[7] = new BlockRegistry.BlockRenderData(renderData);
        }
    }
    @Override
    public boolean canPlace(PlayerEntity player, int x, int y, int z, World world) {
        return true;
    }
    @Override
    public boolean isNoCollide() {
        return false;
    }

    @Override
    public AbstractBlockInstance<LogBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        byte size = 0;
        EFace face = null;
        if(data instanceof DataInputStream stream){
            try {
                size = stream.readByte();
                byte faceData = stream.readByte();
                face = faceData==0?null:EFace.fromId((byte) (faceData-1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(data instanceof String val){
            char faceChar = Character.toLowerCase(val.charAt(0));
            if(faceChar == 'f')
                face = EFace.Front;
            if(faceChar == 'b')
                face = EFace.Back;
            if(faceChar == 'l')
                face = EFace.Left;
            if(faceChar == 'r')
                face = EFace.Right;
            if(faceChar == 'u')
                face = EFace.Up;
            if(faceChar == 'd')
                face = EFace.Down;
            size = (byte) Integer.parseInt(val.substring(1));
        }
        return states[size*7+(face==null?0:(face.id+1))];
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        for(int i = 0;i < 8;i++)
            renderData.put(clientId+i, blockRenderData[i]);
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId+7;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb","log");
    }
    @Override
    public boolean isSerializable() {
        return true;
    }

    public static class LogBlockInstance extends AbstractBlockInstance<LogBlock> implements ISerializable {
        private byte size;
        private EFace face;
        public LogBlockInstance(LogBlock parent, byte size, EFace face) {
            super(parent);
            this.size = size;
            this.face = face;
        }
        @Override
        public int getClientId() {
            return parent.clientId+size;
        }

        @Override
        public void onDestroy() {}
        @Override
        public boolean isValid() {
            return true;
        }
        @Override
        public void onBreak(PlayerEntity player, int x, int y, int z) {
            for(EFace face : EFace.values()){
                BlockPosition neighborPos = new BlockPosition(x + face.xOffset, y + face.yOffset, z + face.zOffset);
                AbstractBlockInstance neighbor = player.getChunk().parent.getBlock(neighborPos);
                if(neighbor.parent == this.parent && ((LogBlockInstance)neighbor).face == face.opposite()){
                    player.breakAsPlayer(neighborPos);
                }
            }
        }
        @Override
        public void onSentToPlayer(PlayerEntity player) {}

        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}

        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {}

        @Override
        public void serialize(DataOutputStream stream) throws IOException {
            stream.writeByte(size);
            stream.writeByte(face==null?0:(face.id+1));
        }

        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return BlockBreakingCalculator.calculateBlockBreakingTime(item, ETool.AXE, 1, 2);
        }

        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            ArrayList<ItemStack> loot = new ArrayList<>();
            loot.add(new ItemStack(player.getChunk().parent.itemRegistry.getItem(Identifier.of("bb","log")), 1));
            return loot;
        }
    }
}
