package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.IFluidContainer;
import com.github.industrialcraft.blockbyteserver.util.MathUtil;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemData;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class BucketItem extends BlockByteItem {
    public final int capacity;
    public BucketItem(Identifier id, int maxStackSize, ItemRenderData itemRenderData, int clientId, int capacity) {
        super(id, maxStackSize, itemRenderData, clientId, null, null);
        this.capacity = capacity;
    }
    @Override
    public ItemData createData(ItemStack is) {
        return new BucketItemData(is, null, 0);
    }
    @Override
    public BarData getBarData(ItemStack itemStack) {
        BucketItemData data = (BucketItemData) itemStack.getData();
        if(data.getFluid() == null)
            return null;
        else
            return new BarData(data.getFluid().color, ((float)data.getFluidAmount())/data.getFluidCapacity());
    }

    @Override
    public boolean onRightClickBlock(ItemStack stack, PlayerEntity player, BlockPosition blockPosition, AbstractBlockInstance blockInstance, boolean shifting) {
        if(blockInstance instanceof FluidBlock.FluidBlockInstance fluidBlockInstance){
            BucketItemData data = (BucketItemData) stack.getData();
            data.setFluid(fluidBlockInstance.parent.fluid, 1000);
            player.updateHand();
            return true;
        }
        return false;
    }

    public static class BucketItemData extends ItemData implements IFluidContainer{
        private Fluid fluidType;
        private int fluidAmount;
        public BucketItemData(ItemStack is, Fluid fluidType, int fluidAmount) {
            super(is);
            this.fluidType = fluidType;
            this.fluidAmount = fluidType==null?0:fluidAmount;
        }
        @Override
        public Fluid getFluid() {
            return fluidType;
        }
        @Override
        public void setFluid(Fluid fluid, int amount) {
            this.fluidType = fluid;
            this.fluidAmount = fluid==null?0:MathUtil.clamp(amount, 0, getFluidCapacity());
        }
        @Override
        public int getFluidAmount() {
            return fluidAmount;
        }
        @Override
        public void setFluidAmount(int amount) {
            if(fluidType != null)
                this.fluidAmount = MathUtil.clamp(amount, 0, getFluidCapacity());
        }
        @Override
        public int getFluidCapacity() {
            return ((BucketItem)getItemStack().getItem()).capacity;
        }

        @Override
        public ItemData clone() {
            return new BucketItemData(getItemStack(), fluidType, fluidAmount);
        }
    }
}
