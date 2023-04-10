package com.github.industrialcraft.blockbyteserver.content;

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
    public BlockByteItem(Identifier id, int maxStackSize, ItemRenderData itemRenderData, int clientId, Identifier place) {
        this.id = id;
        this.maxStackSize = maxStackSize;
        this.itemRenderData = itemRenderData;
        this.clientId = clientId;
        this.place = place;
    }
    public void onRightClick(ItemStack stack, PlayerEntity player, boolean shifting){

    }
    public int getClientId() {
        return clientId;
    }
    @Override
    public int getStackSize() {
        return maxStackSize;
    }
}
