package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

import java.util.LinkedList;

public interface IWorldSERDE {
    void save(Chunk chunk);
    boolean load(Chunk chunk, AbstractBlockInstance[] blocks);
    boolean isChunkSaved(World world, ChunkPosition position);
}
