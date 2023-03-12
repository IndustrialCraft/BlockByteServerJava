package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.BlockInstance;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

public interface IChunkGenerator {
    void generateChunk(BlockInstance[] blocks, ChunkPosition position, World world, Chunk chunk);
}
