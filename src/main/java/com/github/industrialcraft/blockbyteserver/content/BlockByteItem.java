package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.inventorysystem.IItem;

public class BlockByteItem implements IItem {
    public final int maxStackSize;
    public final ItemRenderData itemRenderData;
    public final int clientId;
    public BlockByteItem(int maxStackSize, ItemRenderData itemRenderData, int clientId) {
        this.maxStackSize = maxStackSize;
        this.itemRenderData = itemRenderData;
        this.clientId = clientId;
    }
    public int getClientId() {
        return clientId;
    }
    @Override
    public int getStackSize() {
        return maxStackSize;
    }
}
