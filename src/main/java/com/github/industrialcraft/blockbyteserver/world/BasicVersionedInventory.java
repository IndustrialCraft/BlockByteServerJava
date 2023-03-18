package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.util.ListeningInventory;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemOverflowHandler;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class BasicVersionedInventory extends Inventory implements IInventoryWithSlotVersioning {
    private int[] versions;
    public BasicVersionedInventory(int size, ItemOverflowHandler handler, Object data) {
        super(size, handler, data);
        this.versions = new int[size];
    }
    @Override
    public void setAt(int index, ItemStack itemStack) {
        super.setAt(index, itemStack);
        if(this.versions != null)
            versions[index]++;
    }
    @Override
    public int getVersion(int slot) {
        return this.versions[slot];
    }
}
