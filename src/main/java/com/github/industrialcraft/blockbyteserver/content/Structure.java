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
    private HashMap<BlockPosition,BlockWithData> blocks;
    public Structure(JsonArray json, BlockRegistry blockRegistry) {
        this.blocks = new HashMap<>();
        for (JsonElement blockElement : json) {
            JsonObject block = (JsonObject) blockElement;
            BlockPosition position = new BlockPosition(block.get("x").getAsInt(), block.get("y").getAsInt(), block.get("z").getAsInt());
            JsonElement data = block.get("data");
            blocks.put(position, new BlockWithData(blockRegistry.getBlock(Identifier.parse(block.get("id").getAsString())), data==null?null:data.getAsString()));
        }
    }
    public void place(World world, BlockPosition position, boolean replace){
        for (Map.Entry<BlockPosition, BlockWithData> entry : blocks.entrySet()) {
            BlockPosition targetBlock = new BlockPosition(entry.getKey().x()+position.x(), entry.getKey().y()+position.y(), entry.getKey().z()+position.z());
            if(!replace){
                AbstractBlockInstance block = world.getBlock(targetBlock);
                if(block != null && block.parent != SimpleBlock.AIR)
                    continue;
            }
            world.setBlock(targetBlock, entry.getValue().block, entry.getValue().data);
        }
    }
    private record BlockWithData(AbstractBlock block, String data){}
}
