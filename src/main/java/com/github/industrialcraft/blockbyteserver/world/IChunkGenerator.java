package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.SimpleBlockInstance;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

public interface IChunkGenerator {
    void generateChunk(AbstractBlockInstance[] blocks, ChunkPosition position, World world, Chunk chunk);
    void populate(Chunk chunk);
}
