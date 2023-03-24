package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.util.EHorizontalFace;
import com.github.industrialcraft.blockbyteserver.util.IInventoryBlock;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.inventorysystem.Inventory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChestBlock extends AbstractBlock {
    public final int clientId;
    public final EnumMap<EHorizontalFace, BlockRegistry.BlockRenderData> renderData;
    public ChestBlock(AtomicInteger clientId, BlockRegistry.BlockRenderData northRenderData, BlockRegistry.BlockRenderData southRenderData, BlockRegistry.BlockRenderData leftRenderData, BlockRegistry.BlockRenderData rightRenderData) {
        this.renderData = new EnumMap<>(EHorizontalFace.class);
        this.renderData.put(EHorizontalFace.FRONT, northRenderData);
        this.renderData.put(EHorizontalFace.BACK, southRenderData);
        this.renderData.put(EHorizontalFace.LEFT, leftRenderData);
        this.renderData.put(EHorizontalFace.RIGHT, rightRenderData);
        this.clientId = clientId.get();
        clientId.addAndGet(4);
    }
    @Override
    public AbstractBlockInstance<ChestBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        EHorizontalFace face = EHorizontalFace.FRONT;
        if(data instanceof PlayerEntity player){
            var playerPos = player.getPosition().toBlockPos();
            boolean frontBack = Math.abs(playerPos.z()-z) > Math.abs(playerPos.x()-x);
            if(frontBack){
                if(playerPos.z()-z < 0){
                    face = EHorizontalFace.FRONT;
                } else {
                    face = EHorizontalFace.BACK;
                }
            } else {
                if(playerPos.x()-x < 0){
                    face = EHorizontalFace.LEFT;
                } else {
                    face = EHorizontalFace.RIGHT;
                }
            }
        }
        return new ChestBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), face);
    }
    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        player.setGui(new ChestGUI(player, instance));
        return true;
    }
    @Override
    public LootTable getLootTable() {
        return null;
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        this.renderData.forEach((face, blockRenderData) -> renderData.put(clientId+face.id, blockRenderData));
    }
    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }
    public static class ChestBlockInstance extends AbstractBlockInstance<ChestBlock> implements IInventoryBlock{
        public final int x;
        public final int y;
        public final int z;
        public final BasicVersionedInventory inventory;
        private boolean isValid;
        public final EHorizontalFace face;
        public ChestBlockInstance(ChestBlock parent, int x, int y, int z, EHorizontalFace face) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
            this.inventory = new BasicVersionedInventory(9, (inventory1, is) -> {}, this);
            this.isValid = true;
        }
        @Override
        public int getClientId() {
            return parent.clientId+face.id;
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
        public Inventory getInput(EFace face) {
            return inventory;
        }
        @Override
        public Inventory getOutput(EFace face) {
            return inventory;
        }
    }
    public static class ChestGUI extends InventoryGUI {
        public final AbstractBlockInstance<ChestBlock> block;
        public ChestGUI(PlayerEntity player, AbstractBlockInstance<ChestBlock> block) {
            super(player);
            this.block = block;
            for(int i = 0;i < 9;i++){
                int x = i%3;
                int y = i/3;
                this.slots.put("gui_slot_"+i, new Slot(((ChestBlockInstance)block).inventory, i, -.19f + (x*0.14f), .09f - (y*0.14f)));
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
