package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class World {
    private HashMap<ChunkPosition,Chunk> chunks;
    public final BlockRegistry blockRegistry;
    public final ItemRegistry itemRegistry;
    public final RecipeRegistry recipeRegistry;
    public final EntityRegistry entityRegistry;
    public final FluidRegistry fluidRegistry;
    public final IChunkGenerator chunkGenerator;
    public final IWorldSERDE worldSERDE;
    public World(BlockRegistry blockRegistry, ItemRegistry itemRegistry, RecipeRegistry recipeRegistry, EntityRegistry entityRegistry, FluidRegistry fluidRegistry, IChunkGenerator chunkGenerator, IWorldSERDE worldSERDE) {
        this.blockRegistry = blockRegistry;
        this.itemRegistry = itemRegistry;
        this.recipeRegistry = recipeRegistry;
        this.entityRegistry = entityRegistry;
        this.fluidRegistry = fluidRegistry;
        this.chunkGenerator = chunkGenerator;
        this.worldSERDE = worldSERDE;
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
        List<Chunk> chunksList = new ArrayList<>(chunks.values());
        chunksList.forEach(Chunk::tick);
        for (Chunk chunk : chunksList) {
            if(chunk.shouldUnload()){
                worldSERDE.save(chunk);
            }
        }
        chunks.entrySet().removeIf(entry -> entry.getValue().shouldUnload());
    }
    public void setBlock(BlockPosition blockPosition, AbstractBlock block, Object data){
        ChunkPosition chunkPosition = blockPosition.toChunkPos();
        Chunk chunk = getChunk(chunkPosition);
        if(chunk == null)
            throw new IllegalStateException("Attempting to set block in unloaded chunk");
        chunk.setBlock(block, blockPosition.getChunkXOffset(), blockPosition.getChunkYOffset(), blockPosition.getChunkZOffset(), data);
    }
    public AbstractBlockInstance getBlock(BlockPosition blockPosition){
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
            chunk.load();
        }
        return chunk;
    }
}
