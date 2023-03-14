package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemOverflowHandler;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class ListeningInventory extends Inventory {
    public InventoryChangeListener changeListener;
    public ListeningInventory(int size, ItemOverflowHandler handler, Object data, InventoryChangeListener changeListener) {
        super(size, handler, data);
        this.changeListener = changeListener;
    }
    @Override
    public void setAt(int index, ItemStack itemStack) {
        super.setAt(index, itemStack);
        if(this.changeListener != null)
            this.changeListener.onChange(index, itemStack);
    }
    public void updateSlot(int index){
        if(this.changeListener != null)
            this.changeListener.onChange(index, getAt(index));
    }
    public interface InventoryChangeListener{
        void onChange(int slot, ItemStack item);
    }
}
