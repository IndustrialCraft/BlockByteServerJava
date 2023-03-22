package com.github.industrialcraft.blockbyteserver;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.custom.CrusherMachineBlock;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.net.WSServer;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQuality;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
            return new CrusherMachineBlock(new BlockRegistry.BlockRenderData(blockRenderData), clientId, null);
        });
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.loadDirectory(new File("data/items"));
        RecipeRegistry recipeRegistry = new RecipeRegistry();
        recipeRegistry.registerCreator(Identifier.of("bb", "crushing"), CrusherMachineBlock.CrusherRecipe::new);
        recipeRegistry.loadDirectory(new File("data/recipes"));
        EntityRegistry entityRegistry = new EntityRegistry();
        entityRegistry.register(Identifier.of("bb", "player"), "player.bbmodel", "player", 0.6f, 1.7f, 0.6f);
        entityRegistry.register(Identifier.of("bb", "item"), "item.bbmodel", "", 0.5f, 0.5f, 0.5f);
        World world = new World(blockRegistry, itemRegistry, recipeRegistry, entityRegistry, new IChunkGenerator() {
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
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(new File("content.json").toPath(), gson.toJson(exportContent(blockRegistry, itemRegistry, entityRegistry)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConcurrentLinkedQueue<WebSocket> sockets = new ConcurrentLinkedQueue<>();
        new WSServer(4321, sockets::add).start();
        long livingTicks = 0;
        long startTime = System.currentTimeMillis();
        world.getOrLoadChunk(new Position(0, 40, 0).toBlockPos().toChunkPos());
        new ItemEntity(new Position(0, 40, 0), world, new ItemStack(itemRegistry.getItem(Identifier.of("bb", "grass")), 1));
        while (true){
            WebSocket connection = sockets.poll();
            if(connection != null){
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
    public static JsonObject exportContent(BlockRegistry blockRegistry, ItemRegistry itemRegistry, EntityRegistry entityRegistry){
        JsonObject content = new JsonObject();
        JsonArray blocks = new JsonArray();
        for (Block block : blockRegistry.getBlocks()) {
            JsonObject blockData = new JsonObject();
            blockData.addProperty("id", block.clientId);
            blockData.add("model", block.renderData.json());
            blocks.add(blockData);
        }
        content.add("blocks", blocks);
        JsonArray items = new JsonArray();
        for (BlockByteItem item : itemRegistry.getItems()) {
            JsonObject itemData = new JsonObject();
            itemData.addProperty("id", item.clientId);
            itemData.addProperty("name", item.itemRenderData.name());
            itemData.addProperty("modelType", item.itemRenderData.type());
            if(item.itemRenderData.type().equals("block")){
                itemData.addProperty("modelValue", blockRegistry.getBlock(Identifier.parse(item.itemRenderData.value())).clientId);
            } else {
                itemData.addProperty("modelValue", item.itemRenderData.value());
            }
            items.add(itemData);
        }
        content.add("items", items);
        JsonArray entities = new JsonArray();
        for (EntityRegistry.EntityRenderData entityRenderData : entityRegistry.getEntities()) {
            JsonObject entityData = new JsonObject();
            entityData.addProperty("id", entityRenderData.clientId());
            entityData.addProperty("model", entityRenderData.model());
            entityData.addProperty("texture", entityRenderData.texture());
            entityData.addProperty("hitboxW", entityRenderData.hitboxW());
            entityData.addProperty("hitboxH", entityRenderData.hitboxH());
            entityData.addProperty("hitboxD", entityRenderData.hitboxD());
            entities.add(entityData);
        }
        content.add("entities", entities);
        return content;
    }
}