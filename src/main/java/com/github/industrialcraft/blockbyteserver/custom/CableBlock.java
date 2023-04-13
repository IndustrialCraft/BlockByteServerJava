package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlock;
import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.BlockRegistry;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CableBlock extends AbstractBlock {
    public final int clientId;
    public final BlockRegistry.BlockRenderData[] blockRenderData;
    public CableBlock(AtomicInteger clientId) {
        this.blockRenderData = new BlockRegistry.BlockRenderData[64];
        for(int i = 0;i < 64;i++){
            JsonObject renderData = new JsonObject();
            renderData.addProperty("type", "static");
            renderData.addProperty("texture", "cobble");
            JsonArray models = new JsonArray();
            models.add("cable_middle");
            if((i&1)!=0)
                models.add("cable_front");
            if((i&2)!=0)
                models.add("cable_back");
            if((i&4)!=0)
                models.add("cable_top");
            if((i&8)!=0)
                models.add("cable_bottom");
            if((i&16)!=0)
                models.add("cable_left");
            if((i&32)!=0)
                models.add("cable_right");
            renderData.add("model", models);
            blockRenderData[i] = new BlockRegistry.BlockRenderData(renderData);
        }
        this.clientId = clientId.get();
        clientId.addAndGet(64);
    }

    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        if(instance instanceof CableBlockInstance cableBlockInstance){
            System.out.println("clicked: " + cableBlockInstance.powerGraph);
        }
        return false;
    }

    @Override
    public AbstractBlockInstance<CableBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        byte connectionState = 0;
        if(data instanceof DataInputStream stream){
            try {
                connectionState = stream.readByte();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new CableBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), chunk, connectionState);
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        for(int i = 0;i < 64;i++)
            renderData.put(clientId+i, blockRenderData[i]);
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb","cable");
    }
    @Override
    public boolean isSerializable() {
        return true;
    }

    public static class CableBlockInstance extends AbstractBlockInstance<CableBlock> implements ISerializable, PowerGraph.IPowerGraphComponent {
        public final int x;
        public final int y;
        public final int z;
        public final Chunk chunk;
        private boolean isValid;
        private byte connectionState;
        private PowerGraph powerGraph;
        public CableBlockInstance(CableBlock parent, int x, int y, int z, Chunk chunk, byte connectionState) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.chunk = chunk;
            this.isValid = true;
            this.connectionState = connectionState;
            new PowerGraph().addComponent(this);
        }
        @Override
        public int getClientId() {
            return parent.clientId+connectionState;
        }
        @Override
        public void onDestroy() {
            this.isValid = false;
        }
        @Override
        public boolean isValid() {
            return isValid;
        }

        @Override
        public void onSentToPlayer(PlayerEntity player) {}

        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {
            if(previousInstance.parent == newInstance.parent)
                return;
            if(newInstance instanceof PowerGraph.IPowerGraphComponent powerGraphComponent){
                powerGraphComponent.getPowerGraph().merge(powerGraph);
                connectionState |= 1<<face.id;
            } else {
                powerGraph.split(this);
                connectionState &= ~(1<<face.id);
            }
            updateToClients(chunk, x, y, z);
        }
        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {
            for(EFace face : EFace.values()){
                BlockPosition neighborPosition = new BlockPosition(this.x + face.xOffset, this.y + face.yOffset, this.z + face.zOffset);
                AbstractBlockInstance instance = chunk.parent.getBlock(neighborPosition);
                if(instance instanceof PowerGraph.IPowerGraphComponent powerGraphComponent){
                    if(powerGraphComponent.getPowerGraph() != powerGraph)
                        powerGraphComponent.getPowerGraph().merge(powerGraph);
                    connectionState |= 1<<face.id;
                }
            }
        }

        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return 0;
        }

        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            return null;
        }

        @Override
        public void serialize(DataOutputStream stream) throws IOException {
            stream.writeByte(connectionState);
        }
        @Override
        public PowerGraph getPowerGraph() {
            return powerGraph;
        }
        @Override
        public void setPowerGraph(PowerGraph powerGraph) {
            if(powerGraph == null) {
                powerGraph = new PowerGraph();
                powerGraph.addComponent(this);
            }
            this.powerGraph = powerGraph;
        }
        @Override
        public List<PowerGraph.IPowerGraphComponent> getPowerableNeighbors() {
            ArrayList<PowerGraph.IPowerGraphComponent> powerGraphs = new ArrayList<>();
            for(EFace face : EFace.values()){
                BlockPosition neighborPosition = new BlockPosition(this.x + face.xOffset, this.y + face.yOffset, this.z + face.zOffset);
                AbstractBlockInstance instance = chunk.parent.getBlock(neighborPosition);
                if(instance instanceof PowerGraph.IPowerGraphComponent powerGraphComponent){
                    powerGraphs.add(powerGraphComponent);
                }
            }
            return powerGraphs;
        }
    }
}
