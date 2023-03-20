package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class World {
    private HashMap<ChunkPosition,Chunk> chunks;
    public final BlockRegistry blockRegistry;
    public final ItemRegistry itemRegistry;
    public final RecipeRegistry recipeRegistry;
    public final IChunkGenerator chunkGenerator;
    public World(BlockRegistry blockRegistry, ItemRegistry itemRegistry, RecipeRegistry recipeRegistry, IChunkGenerator chunkGenerator) {
        this.blockRegistry = blockRegistry;
        this.itemRegistry = itemRegistry;
        this.recipeRegistry = recipeRegistry;
        this.chunkGenerator = chunkGenerator;
        this.chunks = new HashMap<>();
    }
    public Entity getEntityByClientId(int clientId){
        for (Chunk chunk : chunks.values()) {
            for (Entity entity : chunk.getEntities()) {
                if(entity.clientId == clientId)
                    return entity;
            }
        }
        return null;
    }
    public void tick(){
        List<Chunk> chunksList = new ArrayList<>();
        for (Chunk value : chunks.values()) {
            chunksList.add(value);
        }
        chunksList.forEach(Chunk::tick);
    }
    public void setBlock(BlockPosition blockPosition, Block block){
        ChunkPosition chunkPosition = blockPosition.toChunkPos();
        Chunk chunk = getChunk(chunkPosition);
        if(chunk == null)
            throw new IllegalStateException("Attempting to set block in unloaded chunk");
        chunk.setBlock(block, blockPosition.getChunkXOffset(), blockPosition.getChunkYOffset(), blockPosition.getChunkZOffset());
    }
    public BlockInstance getBlock(BlockPosition blockPosition){
        ChunkPosition chunkPosition = blockPosition.toChunkPos();
        Chunk chunk = getChunk(chunkPosition);
        if(chunk == null)
            return null;
        return chunk.getBlock(blockPosition.getChunkXOffset(), blockPosition.getChunkYOffset(), blockPosition.getChunkZOffset());
    }
    public Chunk getChunk(ChunkPosition position){
        return this.chunks.get(position);
    }
    public Chunk getOrLoadChunk(ChunkPosition position){
        Chunk chunk = this.chunks.get(position);
        if(chunk == null){
            chunk = new Chunk(this, position);
            chunks.put(position, chunk);
        }
        return chunk;
    }
}
