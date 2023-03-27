package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

public interface IWorldSERDE {
    void save(Chunk chunk);
    void load(Chunk chunk, AbstractBlockInstance[] blocks);
    boolean isChunkSaved(World world, ChunkPosition position);
}
