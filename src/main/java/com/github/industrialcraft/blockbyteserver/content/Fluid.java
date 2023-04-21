package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.Color;
import com.github.industrialcraft.identifier.Identifier;

public class Fluid {
    public final Identifier id;
    public final String name;
    public final Color color;
    public final BlockRegistry.BlockRenderData blockRenderData;
    public Fluid(Identifier id, String name, Color color, BlockRegistry.BlockRenderData blockRenderData) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.blockRenderData = blockRenderData;
    }
    public void registerBlock(BlockRegistry blockRegistry){
        if(blockRenderData != null)
            blockRegistry.loadBlock(id, clientId -> new FluidBlock(clientId, this));
    }
}
