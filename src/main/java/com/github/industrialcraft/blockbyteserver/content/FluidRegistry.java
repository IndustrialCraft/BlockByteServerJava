package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.Color;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FluidRegistry {
    private final HashMap<Identifier, Fluid> fluids;
    public FluidRegistry() {
        this.fluids = new HashMap<>();
    }
    public List<Fluid> getFluids(){
        return new ArrayList<>(this.fluids.values());
    }
    public void loadDirectory(File dir){
        for (File file : dir.listFiles()) {
            if(file.isFile() && file.getName().endsWith(".json")){
                String name = file.getName().replace(".json", "");
                Identifier id = Identifier.parse(name);
                try {
                    FileReader reader = new FileReader(file);
                    loadFluid(id, JsonParser.parseReader(reader).getAsJsonObject());
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public Fluid loadFluid(Identifier id, JsonObject json){
        if(fluids.containsKey(id))
            throw new IllegalStateException("fluid with id " + id + " is already registered");
        JsonArray color = json.getAsJsonArray("color");
        JsonElement block = json.get("block");
        Fluid fluid = new Fluid(id, json.get("name").getAsString(), new Color(color.get(0).getAsFloat(), color.get(1).getAsFloat(), color.get(2).getAsFloat()), (block==null||block.isJsonNull())?null:new BlockRegistry.BlockRenderData((JsonObject) block));
        fluids.put(id, fluid);
        return fluid;
    }
    public void register(Identifier id, Fluid fluid){
        if(fluids.containsKey(id))
            throw new IllegalStateException("fluid with id " + id + " is already registered");
        fluids.put(id, fluid);
    }
    public Fluid getFluid(Identifier id){
        return fluids.get(id);
    }
    public void registerBlocks(BlockRegistry blockRegistry){
        for (Fluid fluid : fluids.values()) {
            fluid.registerBlock(blockRegistry);
        }
    }
}
