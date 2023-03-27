package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleBlock extends AbstractBlock{
    public static SimpleBlock AIR = new SimpleBlock();

    public final BlockRegistry.BlockRenderData renderData;
    public final int clientId;
    public final LootTable lootTable;
    private final SimpleBlockInstance instance;
    public final Identifier identifier;
    public SimpleBlock(BlockRegistry.BlockRenderData renderData, AtomicInteger clientIdGenerator, LootTable lootTable, Identifier identifier) {
        this.renderData = renderData;
        this.clientId = clientIdGenerator.getAndIncrement();
        this.lootTable = lootTable;
        this.identifier = identifier;
        this.instance = new SimpleBlockInstance(this);
    }
    private SimpleBlock(){//air
        this.renderData = null;
        this.clientId = 0;
        this.lootTable = null;
        this.instance = new SimpleBlockInstance(this);
        this.identifier = Identifier.of("bb", "air");
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public AbstractBlockInstance createBlockInstance(Chunk chunk, int x, int y, int z, Object data){
        return instance;
    }
    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player){
        return false;
    }
    @Override
    public LootTable getLootTable() {
        return lootTable;
    }

    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        if(this.renderData != null)
            renderData.put(clientId, this.renderData);
    }
}
