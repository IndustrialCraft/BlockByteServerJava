package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlock;
import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.BlockRegistry;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.InventoryContent;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class BurnerBlock extends AbstractBlock {
    public final int clientId;
    public final EnumMap<EHorizontalFace, BlockRegistry.BlockRenderData> renderData;
    public BurnerBlock(AtomicInteger clientId, BlockRegistry.BlockRenderData northRenderData, BlockRegistry.BlockRenderData southRenderData, BlockRegistry.BlockRenderData leftRenderData, BlockRegistry.BlockRenderData rightRenderData) {
        this.renderData = new EnumMap<>(EHorizontalFace.class);
        this.renderData.put(EHorizontalFace.FRONT, northRenderData);
        this.renderData.put(EHorizontalFace.BACK, southRenderData);
        this.renderData.put(EHorizontalFace.LEFT, leftRenderData);
        this.renderData.put(EHorizontalFace.RIGHT, rightRenderData);
        this.clientId = clientId.get();
        clientId.addAndGet(4);
    }
    @Override
    public AbstractBlockInstance<BurnerBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        EHorizontalFace face = EHorizontalFace.FRONT;
        if(data instanceof BlockPlacementContext placementContext){
            face = placementContext.face;
        }
        InventoryContent inventoryContent = null;
        if(data instanceof DataInputStream stream){
            try {
                inventoryContent = InventorySERDE.deserialize(stream, chunk.parent.itemRegistry);
                face = EHorizontalFace.fromId(stream.readByte());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new ChestBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), chunk.parent, face, inventoryContent);
    }
    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        player.setGui(new ChestGUI(player, instance));
        return true;
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        this.renderData.forEach((face, blockRenderData) -> renderData.put(clientId+face.id, blockRenderData));
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb","chest");
    }
    @Override
    public boolean isSerializable() {
        return true;
    }

    public static class ChestBlockInstance extends AbstractBlockInstance<BurnerBlock> implements IInventoryBlock, ISerializable{
        public final int x;
        public final int y;
        public final int z;
        public final World world;
        public final BasicVersionedInventory inventory;
        private boolean isValid;
        public final EHorizontalFace face;
        public ChestBlockInstance(BurnerBlock parent, int x, int y, int z, World world, EHorizontalFace face, InventoryContent inventoryContent) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.face = face;
            this.inventory = new BasicVersionedInventory(9, (inventory1, is) -> {}, this);
            if(inventoryContent != null)
                this.inventory.loadContent(inventoryContent);
            this.isValid = true;
        }
        @Override
        public int getClientId() {
            return parent.clientId+face.id;
        }
        @Override
        public void onDestroy() {
            for (ItemStack stack : inventory.saveContent().stacks) {
                if(stack != null){
                    var random = ThreadLocalRandom.current();
                    new ItemEntity(new Position(x+0.5f, y+0.5f, z+0.5f), world, stack).addVelocity((random.nextFloat()-.5f)/2, random.nextFloat()/4, (random.nextFloat()-.5f)/2);
                }
            }
            this.isValid = false;
        }
        @Override
        public boolean isValid() {
            return isValid;
        }

        @Override
        public void onSentToPlayer(PlayerEntity player) {}

        @Override
        public Inventory getInput(EFace face) {
            return inventory;
        }
        @Override
        public Inventory getOutput(EFace face) {
            return inventory;
        }

        @Override
        public void onNeighborUpdate(BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {
            System.out.println("neighbor update from " + position);
        }

        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {

        }

        @Override
        public void serialize(DataOutputStream stream) throws IOException {
            InventorySERDE.serialize(stream, inventory.saveContent());
            stream.writeByte(face.id);
        }

        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return 1;
        }

        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            return null;
        }
    }
    public static class ChestGUI extends InventoryGUI {
        public final AbstractBlockInstance<BurnerBlock> block;
        public ChestGUI(PlayerEntity player, AbstractBlockInstance<BurnerBlock> block) {
            super(player, ((ChestBlockInstance)block).inventory);
            this.block = block;
            for(int i = 0;i < 9;i++){
                int x = i%3;
                int y = i/3;
                this.slots.put("gui_slot_"+i, new Slot(((ChestBlockInstance)block).inventory, i, -.19f + (x*0.14f), .09f - (y*0.14f), player.inventory, false));
            }
        }
        @Override
        public boolean onTick() {
            if(!block.isValid())
                return false;
            ChestBlockInstance instance = (ChestBlockInstance) block;
            var blockPos = player.getPosition().toBlockPos();
            int xDiff = instance.x - blockPos.x();
            int yDiff = instance.y - blockPos.y();
            int zDiff = instance.z - blockPos.z();
            return (xDiff*xDiff)+(yDiff*yDiff)+(zDiff*zDiff) < 25;
        }
    }
}
