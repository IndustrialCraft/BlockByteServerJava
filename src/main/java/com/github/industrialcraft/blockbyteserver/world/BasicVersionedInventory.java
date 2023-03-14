package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.util.ListeningInventory;
import com.github.industrialcraft.inventorysystem.ItemOverflowHandler;

public class BasicVersionedInventory extends ListeningInventory implements IInventoryWithSlotVersioning {
    private int[] versions;
    public BasicVersionedInventory(int size, ItemOverflowHandler handler, Object data) {
        super(size, handler, data, null);
        this.versions = new int[size];
        this.changeListener = (slot, item) -> versions[slot]++;
    }
    @Override
    public int getVersion(int slot) {
        return this.versions[slot];
    }
}
