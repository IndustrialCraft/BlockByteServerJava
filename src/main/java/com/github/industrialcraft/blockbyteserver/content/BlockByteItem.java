package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.custom.KnappingScreen;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.Color;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.IItem;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class BlockByteItem implements IItem {
    public final Identifier id;
    public final int maxStackSize;
    public final ItemRenderData itemRenderData;
    public final int clientId;
    public final Identifier place;
    public final KnappingData knappingData;
    public BlockByteItem(Identifier id, int maxStackSize, ItemRenderData itemRenderData, int clientId, Identifier place, KnappingData knappingData) {
        this.id = id;
        this.maxStackSize = maxStackSize;
        this.itemRenderData = itemRenderData;
        this.clientId = clientId;
        this.place = place;
        this.knappingData = knappingData;
    }
    public void onRightClick(ItemStack stack, PlayerEntity player, boolean shifting){
        if(knappingData != null){
            if(stack.getCount() >= knappingData.itemCount) {
                stack.removeCount(knappingData.itemCount);
                player.updateHand();
                player.setGui(new KnappingScreen(player, player.inventory, ((BlockByteItem)stack.getItem()).id, knappingData.bitTexture));
            }
        }
    }
    public boolean onRightClickBlock(ItemStack stack, PlayerEntity player, BlockPosition blockPosition, AbstractBlockInstance blockInstance, boolean shifting){
        return false;
    }
    public int getClientId() {
        return clientId;
    }
    @Override
    public int getStackSize() {
        return maxStackSize;
    }

    public BarData getBarData(ItemStack itemStack){
        return null;
    }

    public record KnappingData(int itemCount, String bitTexture){}
    public record BarData(Color color, float progress){}
}
