package com.github.industrialcraft.blockbyteserver;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.custom.ChestBlock;
import com.github.industrialcraft.blockbyteserver.custom.ConveyorBlock;
import com.github.industrialcraft.blockbyteserver.custom.CrusherMachineBlock;
import com.github.industrialcraft.blockbyteserver.net.WSServer;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.ISerializable;
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

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockByteServerMain {
    public static void main(String[] args) {
        new File("world").mkdir();
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
        blockRegistry.loadBlock(Identifier.of("bb", "chest"), clientId -> {
            JsonObject northBlockRenderData = new JsonObject();
            northBlockRenderData.addProperty("type", "cube");
            northBlockRenderData.addProperty("north", "chest_front");
            northBlockRenderData.addProperty("south", "chest_side");
            northBlockRenderData.addProperty("left", "chest_side");
            northBlockRenderData.addProperty("right", "chest_side");
            northBlockRenderData.addProperty("up", "chest_base");
            northBlockRenderData.addProperty("down", "chest_base");
            JsonObject southBlockRenderData = new JsonObject();
            southBlockRenderData.addProperty("type", "cube");
            southBlockRenderData.addProperty("north", "chest_side");
            southBlockRenderData.addProperty("south", "chest_front");
            southBlockRenderData.addProperty("left", "chest_side");
            southBlockRenderData.addProperty("right", "chest_side");
            southBlockRenderData.addProperty("up", "chest_base");
            southBlockRenderData.addProperty("down", "chest_base");
            JsonObject leftBlockRenderData = new JsonObject();
            leftBlockRenderData.addProperty("type", "cube");
            leftBlockRenderData.addProperty("north", "chest_side");
            leftBlockRenderData.addProperty("south", "chest_side");
            leftBlockRenderData.addProperty("left", "chest_front");
            leftBlockRenderData.addProperty("right", "chest_side");
            leftBlockRenderData.addProperty("up", "chest_base");
            leftBlockRenderData.addProperty("down", "chest_base");
            JsonObject rightBlockRenderData = new JsonObject();
            rightBlockRenderData.addProperty("type", "cube");
            rightBlockRenderData.addProperty("north", "chest_side");
            rightBlockRenderData.addProperty("south", "chest_side");
            rightBlockRenderData.addProperty("left", "chest_side");
            rightBlockRenderData.addProperty("right", "chest_front");
            rightBlockRenderData.addProperty("up", "chest_base");
            rightBlockRenderData.addProperty("down", "chest_base");
            return new ChestBlock(clientId, new BlockRegistry.BlockRenderData(northBlockRenderData), new BlockRegistry.BlockRenderData(southBlockRenderData), new BlockRegistry.BlockRenderData(leftBlockRenderData), new BlockRegistry.BlockRenderData(rightBlockRenderData));
        });
        blockRegistry.loadBlock(Identifier.of("bb", "conveyor"), clientId -> {
            JsonObject blockRenderData = new JsonObject();
            blockRenderData.addProperty("type", "static");
            blockRenderData.addProperty("model", "");
            blockRenderData.addProperty("texture", "");
            blockRenderData.addProperty("north", false);
            blockRenderData.addProperty("south", false);
            blockRenderData.addProperty("left", false);
            blockRenderData.addProperty("right", false);
            blockRenderData.addProperty("up", false);
            blockRenderData.addProperty("down", false);
            var renderData = new BlockRegistry.BlockRenderData(blockRenderData);
            return new ConveyorBlock(clientId, renderData, renderData, renderData, renderData);
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
            public void generateChunk(AbstractBlockInstance[] blocks, ChunkPosition position, World world, Chunk chunk) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        //System.out.println(((position.x()*16)+x) + ":" + ((position.z()*16)+z) + ":" + noise.evaluateNoise((position.x()*16)+x, (position.z()*16)+z, 10));
                        float scale = 0.05f;
                        int height = (int) (Noise.gradientCoherentNoise3D((((position.x() * 16) + x) * scale), (((position.z() * 16) + z) * scale), 0, 4321, NoiseQuality.FAST) * 30);
                        for (int y = 0; y < 16; y++) {
                            AbstractBlock block;
                            if (y + (position.y() * 16) < height + 20 - 5) {
                                block = world.blockRegistry.getBlock(Identifier.of("bb", "cobble"));
                            } else if (y + (position.y() * 16) < height + 20 - 1) {
                                block = world.blockRegistry.getBlock(Identifier.of("bb", "dirt"));
                            } else if (y + (position.y() * 16) < height + 20) {
                                block = world.blockRegistry.getBlock(Identifier.of("bb", "grass"));
                            } else {
                                block = SimpleBlock.AIR;
                            }
                            blocks[x + (y * 16) + z * (16 * 16)] = block.createBlockInstance(chunk, x, y, z, null);
                        }
                    }
                }
            }
        }, new IWorldSERDE() {
            @Override
            public void save(Chunk chunk) {
                try {
                    FileOutputStream fstream = new FileOutputStream("world/chunk" + chunk.position.x() + "," + chunk.position.y() + "," + chunk.position.z());
                    DataOutputStream stream = new DataOutputStream(fstream);
                    int idGenerator = -1;
                    HashMap<Identifier,Integer> idMap = new HashMap<>();
                    ByteArrayOutputStream blocksRaw = new ByteArrayOutputStream();
                    DataOutputStream blocks = new DataOutputStream(blocksRaw);
                    for(int i = 0;i < 4096;i++){
                        AbstractBlockInstance instance = chunk.getUnsafeBlocks()[i];
                        Identifier id = instance.parent.getIdentifier();
                        Integer saveId = idMap.get(id);
                        int blockId;
                        if(saveId == null){
                            idGenerator++;
                            idMap.put(id, idGenerator);
                            blockId = idGenerator;
                        } else {
                            blockId = saveId;
                        }
                        blocks.writeInt(blockId);
                        if(instance instanceof ISerializable serializable){
                            ByteArrayOutputStream blockData = new ByteArrayOutputStream();
                            serializable.serialize(new DataOutputStream(blockData));
                            byte[] blockByteData = blockData.toByteArray();
                            blocks.writeInt(blockByteData.length);
                            blocks.write(blockByteData);
                        }
                    }
                    stream.writeInt(idMap.size());
                    for (Map.Entry<Identifier, Integer> entry : idMap.entrySet()) {
                        stream.writeInt(entry.getValue());
                        stream.writeUTF(entry.getKey().toString());
                    }
                    stream.write(blocksRaw.toByteArray());
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void load(Chunk chunk, AbstractBlockInstance[] blocks) {
                try {
                    FileInputStream fstream = new FileInputStream("world/chunk" + chunk.position.x() + "," + chunk.position.y() + "," + chunk.position.z());
                    BufferedInputStream bstream = new BufferedInputStream(fstream);
                    DataInputStream stream = new DataInputStream(bstream);
                    HashMap<Integer,Identifier> idMap = new HashMap<>();
                    int size = stream.readInt();
                    for(int i = 0;i < size;i++){
                        idMap.put(stream.readInt(), Identifier.parse(stream.readUTF()));
                    }
                    for(int z = 0;z < 16;z++) {
                        for (int y = 0; y < 16; y++) {
                            for (int x = 0; x < 16; x++) {
                                Identifier blockId = idMap.get(stream.readInt());
                                AbstractBlock block = chunk.parent.blockRegistry.getBlock(blockId);
                                Object data = null;
                                if(block.isSerializable()){
                                    int length = stream.readInt();
                                    ByteArrayInputStream blockData = new ByteArrayInputStream(stream.readNBytes(length));
                                    data = new DataInputStream(blockData);
                                }
                                blocks[x + (y * 16) + z * (16 * 16)] = block.createBlockInstance(chunk, x, y, z, data);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public boolean isChunkSaved(World world, ChunkPosition position) {
                return new File("world/chunk" + position.x() + "," + position.y() + "," + position.z()).exists();
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
        content.add("blocks", blockRegistry.getBlocksRenderData());
        JsonArray items = new JsonArray();
        for (BlockByteItem item : itemRegistry.getItems()) {
            JsonObject itemData = new JsonObject();
            itemData.addProperty("id", item.clientId);
            itemData.addProperty("name", item.itemRenderData.name());
            itemData.addProperty("modelType", item.itemRenderData.type());
            if(item.itemRenderData.type().equals("block")){
                itemData.addProperty("modelValue", blockRegistry.getBlock(Identifier.parse(item.itemRenderData.value())).getDefaultClientId());
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