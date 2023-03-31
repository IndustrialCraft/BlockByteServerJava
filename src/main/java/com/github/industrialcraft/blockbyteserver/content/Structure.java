package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Structure {
    private HashMap<BlockPosition,AbstractBlock> blocks;
    public Structure(JsonArray json, BlockRegistry blockRegistry) {
        this.blocks = new HashMap<>();
        for (JsonElement blockElement : json) {
            JsonObject block = (JsonObject) blockElement;
            BlockPosition position = new BlockPosition(block.get("x").getAsInt(), block.get("y").getAsInt(), block.get("z").getAsInt());
            blocks.put(position, blockRegistry.getBlock(Identifier.parse(block.get("id").getAsString())));
        }
    }
    public void place(World world, BlockPosition position){
        for (Map.Entry<BlockPosition, AbstractBlock> entry : blocks.entrySet()) {
            world.setBlock(new BlockPosition(entry.getKey().x()+position.x(), entry.getKey().y()+position.y(), entry.getKey().z()+position.z()), entry.getValue(), null);
        }
    }
}
