package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class BlockRegistry {
    private final HashMap<Identifier, Block> blocks;
    private int clientIds;
    public BlockRegistry() {
        this.blocks = new HashMap<>();
        this.clientIds = 1;
    }
    public List<Block> getBlocks(){
        return new ArrayList<>(this.blocks.values());
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
    public Block loadBlock(Identifier id, JsonObject json){
        if(blocks.containsKey(id))
            throw new IllegalStateException("block with id " + id + " is already registered");
        JsonObject model = json.getAsJsonObject("model");
        MessageS2C.InitializeContent.BlockRenderData renderData = new MessageS2C.InitializeContent.BlockRenderData(model);
        JsonObject lootTableJson = json.getAsJsonObject("loot");
        Block block = new Block(renderData, clientIds, lootTableJson==null?null:new LootTable(lootTableJson));
        clientIds++;
        blocks.put(id, block);
        return block;
    }
    public void loadBlock(Identifier id, Function<Integer,Block> creator){
        if(blocks.containsKey(id))
            throw new IllegalStateException("block with id " + id + " is already registered");
        Block block = creator.apply(clientIds);
        clientIds++;
        blocks.put(id, block);
    }
    public Block getBlock(Identifier id){
        return blocks.get(id);
    }
}
