package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.ETool;
import com.github.industrialcraft.blockbyteserver.util.ITool;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ItemRegistry {
    private final HashMap<Identifier, BlockByteItem> items;
    private int clientIds;
    public ItemRegistry() {
        this.items = new HashMap<>();
        this.clientIds = 0;
    }
    public List<BlockByteItem> getItems(){
        return new ArrayList<>(this.items.values());
    }
    public void loadDirectory(File dir){
        for (File file : dir.listFiles()) {
            if(file.isFile() && file.getName().endsWith(".json")){
                String name = file.getName().replace(".json", "");
                Identifier id = Identifier.parse(name);
                try {
                    FileReader reader = new FileReader(file);
                    loadItem(id, JsonParser.parseReader(reader).getAsJsonObject());
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public BlockByteItem loadItem(Identifier id, JsonObject json){
        if(items.containsKey(id))
            throw new IllegalStateException("item with id " + id + " is already registered");
        JsonObject model = json.getAsJsonObject("model");
        ItemRenderData renderData = new ItemRenderData(json.get("name").getAsString(), model.get("type").getAsString(), model.get("value").getAsString());
        JsonElement place = json.get("place");
        JsonObject tool = (JsonObject) json.get("tool");
        BlockByteItem item;
        if(tool != null){
            item = new BlockByteToolItem(id, json.get("stackSize").getAsInt(), renderData, clientIds, place == null ? null : Identifier.parse(place.getAsString())){
                @Override
                public ETool getToolType() {
                    return ETool.fromString(tool.get("type").getAsString());
                }
                @Override
                public float getToolSpeed() {
                    return tool.get("speed").getAsFloat();
                }
                @Override
                public int getToolLevel() {
                    return tool.get("level").getAsInt();
                }
            };
        } else {
            item = new BlockByteItem(id, json.get("stackSize").getAsInt(), renderData, clientIds, place == null ? null : Identifier.parse(place.getAsString()));
        }
        clientIds++;
        items.put(id, item);
        return item;
    }
    public void register(Identifier id, Function<Integer,BlockByteItem> creator){
        if(items.containsKey(id))
            throw new IllegalStateException("item with id " + id + " is already registered");
        clientIds++;
        items.put(id, creator.apply(clientIds));
    }
    public BlockByteItem getItem(Identifier id){
        return items.get(id);
    }
    public abstract class BlockByteToolItem extends BlockByteItem implements ITool{
        public BlockByteToolItem(Identifier id, int maxStackSize, ItemRenderData itemRenderData, int clientId, Identifier place) {
            super(id, maxStackSize, itemRenderData, clientId, place);
        }
    }
}
