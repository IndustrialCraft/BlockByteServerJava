package com.github.industrialcraft.blockbyteserver;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.custom.*;
import com.github.industrialcraft.blockbyteserver.net.WSServer;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.blockbyteserver.world.gen.WorldGenerator;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.*;
import org.java_websocket.WebSocket;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockByteServerMain {
    public static void main(String[] args) throws IOException {
        new File("world").mkdir();
        BlockRegistry blockRegistry = new BlockRegistry();
        FluidRegistry fluidRegistry = new FluidRegistry();
        fluidRegistry.loadDirectory(new File("data/fluids"));
        fluidRegistry.registerBlocks(blockRegistry);
        blockRegistry.loadDirectory(new File("data/blocks"));
        blockRegistry.loadBlock(Identifier.of("bb", "log"), LogBlock::new);
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
        blockRegistry.loadBlock(Identifier.of("bb", "cable"), CableBlock::new);
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
        blockRegistry.loadBlock(Identifier.of("bb", "fire_pit"), FirePitBlock::new);
        blockRegistry.loadBlock(Identifier.of("bb", "wet_mud_brick"), WetMudBrickBlock::new);
        blockRegistry.loadBlock(Identifier.of("bb", "log_pile"), LogPileBlock::new);
        blockRegistry.postInit();
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.loadDirectory(new File("data/items"));
        itemRegistry.register(Identifier.of("bb", "claybucket"), clientId -> new BucketItem(Identifier.of("bb", "claybucket"), 1, new ItemRenderData("clay bucket", "texture", "clay_bucket"), clientId, 100));
        RecipeRegistry recipeRegistry = new RecipeRegistry();
        recipeRegistry.registerCreator(Identifier.of("bb", "crushing"), CrusherMachineBlock.CrusherRecipe::new);
        recipeRegistry.registerCreator(Identifier.of("bb", "knapping"), (id, json) -> new KnappingScreen.KnappingRecipe(id, json, itemRegistry));
        recipeRegistry.registerCreator(Identifier.of("bb", "crafting"), (id, json) -> new PlayerInventoryGUI.CraftingRecipe(id, json, itemRegistry, fluidRegistry));
        recipeRegistry.registerCreator(Identifier.of("bb", "fire_pit"), FirePitBlock.FirePitRecipe::new);
        recipeRegistry.loadDirectory(new File("data/recipes"));
        EntityRegistry entityRegistry = new EntityRegistry();
        entityRegistry.register(Identifier.of("bb", "player"), "player.bbm", "player", 0.6f, 1.7f, 0.6f);
        entityRegistry.register(Identifier.of("bb", "item"), "item.bbmodel", "", 0.5f, 0.5f, 0.5f);
        World world = new World(blockRegistry, itemRegistry, recipeRegistry, entityRegistry, fluidRegistry, new WorldGenerator(blockRegistry, 5555), new IWorldSERDE() {
            @Override
            public void save(Chunk chunk) {
                try {
                    FileOutputStream fstream = new FileOutputStream("world/chunk" + chunk.position.x() + "," + chunk.position.y() + "," + chunk.position.z());
                    DataOutputStream stream = new DataOutputStream(fstream);
                    stream.writeBoolean(chunk.isPopulated());
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
            public boolean load(Chunk chunk, AbstractBlockInstance[] blocks) {
                try {
                    FileInputStream fstream = new FileInputStream("world/chunk" + chunk.position.x() + "," + chunk.position.y() + "," + chunk.position.z());
                    BufferedInputStream bstream = new BufferedInputStream(fstream);
                    DataInputStream stream = new DataInputStream(bstream);
                    boolean populated = stream.readBoolean();
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
                                int offset = x + (y * 16) + z * (16 * 16);
                                AbstractBlockInstance blockInstance = block.createBlockInstance(chunk, x, y, z, data);
                                blocks[offset] = blockInstance;
                            }
                        }
                    }
                    return populated;
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