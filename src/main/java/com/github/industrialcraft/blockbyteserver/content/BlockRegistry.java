package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.ETool;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BlockRegistry {
    private final HashMap<Identifier, AbstractBlock> blocks;
    private AtomicInteger clientIds;
    public BlockRegistry() {
        this.blocks = new HashMap<>();
        this.clientIds = new AtomicInteger(1);
        blocks.put(Identifier.of("bb","air"), SimpleBlock.AIR);
    }
    public JsonArray getBlocksRenderData(){
        JsonArray blocks = new JsonArray();
        HashMap<Integer,BlockRenderData> renderData = new HashMap<>();
        for (AbstractBlock block : this.blocks.values()) {
            block.registerRenderData(renderData);
        }
        for (Map.Entry<Integer, BlockRenderData> entry : renderData.entrySet()) {
            JsonObject block = new JsonObject();
            block.addProperty("id", entry.getKey());
            block.add("model", entry.getValue().json());
            blocks.add(block);
        }
        return blocks;
    }
    public void loadDirectory(File dir){
        for (File file : dir.listFiles()) {
            if(file.isFile() && file.getName().endsWith(".json")){
                String name = file.getName().replace(".json", "");
                Identifier id = Identifier.parse(name);
                try {
                    FileReader reader = new FileReader(file);
                    loadBlock(id, JsonParser.parseReader(reader).getAsJsonObject());
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public SimpleBlock loadBlock(Identifier id, JsonObject json){
        if(blocks.containsKey(id))
            throw new IllegalStateException("block with id " + id + " is already registered");
        JsonObject model = json.getAsJsonObject("model");
        BlockRenderData renderData = new BlockRenderData(model);
        JsonObject lootTableJson = json.getAsJsonObject("loot");
        JsonElement tool = json.get("toolType");
        JsonElement minToolLevel = json.get("minToolLevel");
        JsonElement blockHardness = json.get("blockHardness");
        SimpleBlock block = new SimpleBlock(renderData, clientIds, lootTableJson==null?null:new LootTable(lootTableJson), id, tool==null?null:ETool.fromString(tool.getAsString()), minToolLevel==null?0:minToolLevel.getAsInt(), blockHardness==null?1f:blockHardness.getAsFloat());
        blocks.put(id, block);
        return block;
    }
    public void loadBlock(Identifier id, Function<AtomicInteger, AbstractBlock> creator){
        if(blocks.containsKey(id))
            throw new IllegalStateException("block with id " + id + " is already registered");
        AbstractBlock block = creator.apply(clientIds);
        blocks.put(id, block);
    }
    public AbstractBlock getBlock(Identifier id){
        return blocks.get(id);
    }
    public record BlockRenderData(JsonObject json){}
    public void postInit(){
        for (AbstractBlock value : this.blocks.values()) {
            value.postInit(this);
        }
    }
}
