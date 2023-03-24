package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlock;
import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.BlockRegistry;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConveyorBlock extends AbstractBlock {
    public final int clientId;
    public final EnumMap<EHorizontalFace, BlockRegistry.BlockRenderData> renderData;
    public ConveyorBlock(AtomicInteger clientId, BlockRegistry.BlockRenderData northRenderData, BlockRegistry.BlockRenderData southRenderData, BlockRegistry.BlockRenderData leftRenderData, BlockRegistry.BlockRenderData rightRenderData) {
        this.renderData = new EnumMap<>(EHorizontalFace.class);
        this.renderData.put(EHorizontalFace.FRONT, northRenderData);
        this.renderData.put(EHorizontalFace.BACK, southRenderData);
        this.renderData.put(EHorizontalFace.LEFT, leftRenderData);
        this.renderData.put(EHorizontalFace.RIGHT, rightRenderData);
        this.clientId = clientId.get();
        clientId.addAndGet(4);
    }
    @Override
    public AbstractBlockInstance<ConveyorBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
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
        return new ConveyorBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), face.opposite(), chunk.parent);
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
    public static class ConveyorBlockInstance extends AbstractBlockInstance<ConveyorBlock> implements ITicking, IInventoryBlock {
        public final int x;
        public final int y;
        public final int z;
        public final Inventory inputInventory;
        public final Inventory outputInventory;
        private boolean isValid;
        public final EHorizontalFace face;
        private int transferDelay;
        public final World world;
        public ConveyorBlockInstance(ConveyorBlock parent, int x, int y, int z, EHorizontalFace face, World world) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
            this.world = world;
            this.inputInventory = new Inventory(1, (inventory1, is) -> System.out.println("dropped"), this);
            this.outputInventory = new Inventory(1, (inventory1, is) -> {}, this);
            this.isValid = true;
            this.transferDelay = -1;
            this.inputInventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","cobble")), 1));
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
        public void tick() {
            if(outputInventory.getAt(0) != null){
                BlockPosition targetPosition = new BlockPosition(x+face.xOffset, y, z+face.zOffset);
                AbstractBlockInstance block = world.getBlock(targetPosition);
                if(block instanceof IInventoryBlock inventoryBlock){
                    Inventory input = inventoryBlock.getInput(face.fullFace.opposite());
                    if(input != null){
                        input.addItem(outputInventory.getAt(0));
                        outputInventory.setAt(0, null);
                    }
                }
            }
            if(inputInventory.getAt(0) == null){
                BlockPosition targetPosition = new BlockPosition(x-face.xOffset, y, z-face.zOffset);
                AbstractBlockInstance block = world.getBlock(targetPosition);
                if(block instanceof IInventoryBlock inventoryBlock){
                    Inventory output = inventoryBlock.getOutput(face.fullFace);
                    if(output != null){
                        if(output.getAt(0) != null){
                            inputInventory.setAt(0, output.getAt(0));
                            output.setAt(0, null);
                        }
                    }
                }
            }
            if(inputInventory.getAt(0) != null && transferDelay == -1 && outputInventory.getAt(0) == null){
                transferDelay = 20;
            }
            if(transferDelay == 0){
                outputInventory.setAt(0, inputInventory.getAt(0));
                inputInventory.setAt(0, null);
            }
            if(transferDelay >= 0){
                transferDelay--;
            }
        }
        @Override
        public Inventory getInput(EFace face) {
            return inputInventory;
        }
        @Override
        public Inventory getOutput(EFace face) {
            return outputInventory;
        }
    }
}
