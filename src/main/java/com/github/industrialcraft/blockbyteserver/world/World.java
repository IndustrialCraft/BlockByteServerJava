package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.BlockRegistry;
import com.github.industrialcraft.blockbyteserver.content.Block;
import com.github.industrialcraft.blockbyteserver.content.BlockInstance;
import com.github.industrialcraft.blockbyteserver.content.ItemRegistry;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class World {
    private HashMap<ChunkPosition,Chunk> chunks;
    public final BlockRegistry blockRegistry;
    public final ItemRegistry itemRegistry;
    public World(BlockRegistry blockRegistry, ItemRegistry itemRegistry) {
        this.blockRegistry = blockRegistry;
        this.itemRegistry = itemRegistry;
        this.chunks = new HashMap<>();
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
