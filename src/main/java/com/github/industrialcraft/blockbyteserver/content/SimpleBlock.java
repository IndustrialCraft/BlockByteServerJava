package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ETool;
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
    public final ETool tool;
    public final int minToolLevel;
    public final float blockHardness;
    public final boolean needsSupport;
    public SimpleBlock(BlockRegistry.BlockRenderData renderData, AtomicInteger clientIdGenerator, LootTable lootTable, Identifier identifier, ETool tool, int minToolLevel, float blockHardness, boolean needsSupport) {
        this.renderData = renderData;
        this.clientId = clientIdGenerator.getAndIncrement();
        this.lootTable = lootTable;
        this.identifier = identifier;
        this.tool = tool;
        this.minToolLevel = minToolLevel;
        this.blockHardness = blockHardness;
        this.needsSupport = needsSupport;
        this.instance = new SimpleBlockInstance(this);
    }
    private SimpleBlock(){//air
        this.renderData = null;
        this.clientId = 0;
        this.lootTable = null;
        this.instance = new SimpleBlockInstance(this);
        this.identifier = Identifier.of("bb", "air");
        this.tool = null;
        this.minToolLevel = 0;
        this.blockHardness = 0;
        this.needsSupport = false;
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
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        if(this.renderData != null)
            renderData.put(clientId, this.renderData);
    }
}
