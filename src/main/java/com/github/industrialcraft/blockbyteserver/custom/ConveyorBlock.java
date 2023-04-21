package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlock;
import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.content.BlockRegistry;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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
        if(data instanceof BlockPlacementContext placementContext){
            face = placementContext.face;
        }
        return new ConveyorBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), face.opposite(), chunk);
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
        return Identifier.of("bb","conveyor");
    }
    @Override
    public boolean canPlace(PlayerEntity player, int x, int y, int z, World world) {
        return true;
    }
    @Override
    public boolean isNoCollide() {
        return true;
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
        public final Chunk chunk;
        public ConveyorBlockInstance(ConveyorBlock parent, int x, int y, int z, EHorizontalFace face, Chunk chunk) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
            this.chunk = chunk;
            this.inputInventory = new Inventory(1, (inventory1, is) -> {}, this){
                @Override
                public ItemStack setAt(int index, ItemStack itemStack) {
                    itemStack = super.setAt(index, itemStack);
                    MessageS2C message;
                    if(itemStack != null){
                        var point = getInputPoint();
                        message = new MessageS2C.BlockAddItem(x, y, z, point.x(), 0, point.y(), 0, ((BlockByteItem)itemStack.getItem()).getClientId());
                    } else {
                        message = new MessageS2C.BlockRemoveItem(x, y, z, 0);
                    }
                    chunk.announceToViewersExcept(message, null);
                    return itemStack;
                }
            };
            this.outputInventory = new Inventory(1, (inventory1, is) -> {}, this){
                @Override
                public ItemStack setAt(int index, ItemStack itemStack) {
                    itemStack = super.setAt(index, itemStack);
                    MessageS2C message;
                    if(itemStack != null){
                        var point = getOutputPoint();
                        message = new MessageS2C.BlockAddItem(x, y, z, point.x(), 0, point.y(), 1, ((BlockByteItem)itemStack.getItem()).getClientId());
                    } else {
                        message = new MessageS2C.BlockRemoveItem(x, y, z, 1);
                    }
                    chunk.announceToViewersExcept(message, null);
                    return itemStack;
                }
            };
            this.isValid = true;
            this.transferDelay = -1;
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
        public Point2D getInputPoint(){
            return new Point2D(0.25f + (face.xOffset/2f) - (face.xOffset*(transferDelay/20f)), 0.25f + (face.zOffset/2f) - (face.zOffset*(transferDelay/20f)));
        }
        public Point2D getOutputPoint(){
            return new Point2D(0.25f + (face.xOffset/2f), 0.25f + (face.zOffset/2f));
        }
        @Override
        public void onSentToPlayer(PlayerEntity player) {
            ItemStack inputItem = inputInventory.getAt(0);
            if(inputItem != null) {
                var point = getInputPoint();
                player.send(new MessageS2C.BlockAddItem(x, y, z, point.x(), 0, point.y(), 0, ((BlockByteItem) inputItem.getItem()).getClientId()));
            }
            ItemStack outputItem = outputInventory.getAt(0);
            if(outputItem != null){
                var point = getOutputPoint();
                player.send(new MessageS2C.BlockAddItem(x, y, z, point.x(), 0, point.y(), 1, ((BlockByteItem)outputItem.getItem()).getClientId()));
            }
        }
        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}

        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {

        }

        @Override
        public void tick() {
            if(outputInventory.getAt(0) != null){
                BlockPosition targetPosition = new BlockPosition(x+face.xOffset, y, z+face.zOffset);
                AbstractBlockInstance block = chunk.parent.getBlock(targetPosition);
                if(block instanceof IInventoryBlock inventoryBlock){
                    Inventory input = inventoryBlock.getInput(face.fullFace.opposite());
                    if(input != null){
                        ItemStack item = outputInventory.getAt(0);
                        if(input.getRemainingSpaceFor(item) >= 1) {
                            input.addItem(item);
                            outputInventory.setAt(0, null);
                        }
                    }
                }
            }
            if(inputInventory.getAt(0) == null){
                BlockPosition targetPosition = new BlockPosition(x-face.xOffset, y, z-face.zOffset);
                AbstractBlockInstance block = chunk.parent.getBlock(targetPosition);
                if(block instanceof IInventoryBlock inventoryBlock){
                    Inventory output = inventoryBlock.getOutput(face.fullFace);
                    if(output != null){
                        List<ItemStack> removed = output.removeItems(item -> true, 1);
                        if(removed != null){
                            inputInventory.setAt(0, removed.get(0));
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
                if(transferDelay >= 0) {
                    var point = getInputPoint();
                    chunk.announceToViewersExcept(new MessageS2C.BlockMoveItem(x, y, z, point.x(), 0, point.y(), 0), null);
                }
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

        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return 1;
        }

        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            return null;
        }
    }
}
