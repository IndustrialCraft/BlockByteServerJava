package com.github.industrialcraft.blockbyteserver;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.custom.CrusherMachineBlock;
import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.net.WSServer;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQuality;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockByteServerMain {
    public static void main(String[] args) {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.loadDirectory(new File("data/blocks"));
        blockRegistry.loadBlock(Identifier.of("bb", "crusher"), clientId -> {
            JsonObject blockRenderData = new JsonObject();
            blockRenderData.addProperty("type", "cube");
            blockRenderData.addProperty("north", "grass_side");
            blockRenderData.addProperty("south", "grass_side");
            blockRenderData.addProperty("left", "grass_side");
            blockRenderData.addProperty("right", "grass_side");
            blockRenderData.addProperty("up", "grass_side");
            blockRenderData.addProperty("down", "grass_side");
            return new CrusherMachineBlock(new MessageS2C.InitializeContent.BlockRenderData(blockRenderData), clientId, null);
        });
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.loadDirectory(new File("data/items"));
        World world = new World(blockRegistry, itemRegistry, new IChunkGenerator() {
            @Override
            public void generateChunk(BlockInstance[] blocks, ChunkPosition position, World world, Chunk chunk) {
                for(int x = 0;x < 16;x++){
                    for(int z = 0;z < 16;z++){
                        //System.out.println(((position.x()*16)+x) + ":" + ((position.z()*16)+z) + ":" + noise.evaluateNoise((position.x()*16)+x, (position.z()*16)+z, 10));
                        float scale = 0.05f;
                        int height = (int) (Noise.gradientCoherentNoise3D((((position.x()*16)+x)*scale),(((position.z()*16)+z)*scale), 0, 4321, NoiseQuality.FAST) * 30);
                        for(int y = 0;y < 16;y++){
                            Block block;
                            if(y+(position.y()*16) < height+20-5){
                                block = world.blockRegistry.getBlock(Identifier.of("bb", "cobble"));
                            } else if(y+(position.y()*16) < height+20-1){
                                block = world.blockRegistry.getBlock(Identifier.of("bb", "dirt"));
                            } else if(y+(position.y()*16) < height+20){
                                block = world.blockRegistry.getBlock(Identifier.of("bb", "grass"));
                            } else {
                                block = Block.AIR;
                            }
                            blocks[x+(y*16)+z*(16*16)] = block.createBlockInstance(chunk, x, y, z);
                        }
                    }
                }
            }
        });
        ConcurrentLinkedQueue<WebSocket> sockets = new ConcurrentLinkedQueue<>();
        new WSServer(4321, sockets::add).start();
        long livingTicks = 0;
        long startTime = System.currentTimeMillis();
        while (true){
            WebSocket connection = sockets.poll();
            if(connection != null){
                ArrayList<MessageS2C.InitializeContent.BlockRenderData> renderData = new ArrayList<>();
                List<Block> blocks = blockRegistry.getBlocks();
                blocks.sort(Comparator.comparingInt(Block::getClientId));
                blocks.forEach(block -> renderData.add(block.renderData));
                ArrayList<MessageS2C.InitializeContent.EntityRenderData> entityRenderData = new ArrayList<>();
                ArrayList<ItemRenderData> itemRenderData = new ArrayList<>();
                List<BlockByteItem> items = itemRegistry.getItems();
                items.sort(Comparator.comparingInt(BlockByteItem::getClientId));
                items.forEach(item -> itemRenderData.add(item.itemRenderData));
                entityRenderData.add(new MessageS2C.InitializeContent.EntityRenderData("player", "player"));
                try {
                    connection.send(new MessageS2C.InitializeContent(renderData, entityRenderData, itemRenderData, blockRegistry).toBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Position spawnPos = new Position(0, 50, 0);
                world.getOrLoadChunk(spawnPos.toBlockPos().toChunkPos());
                PlayerEntity playerEntity = new PlayerEntity(spawnPos, world, connection);
                connection.setAttachment(playerEntity);
            }
            world.tick();
            while(((livingTicks*50)+startTime) > System.currentTimeMillis()){
                Thread.yield();
            }
            livingTicks++;
        }
    }
}