package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class ItemEntity extends Entity{
    public final ItemStack item;
    public ItemEntity(Position position, World world, ItemStack item) {
        super(position, world);
        this.item = item;
    }
    @Override
    public int getClientType() {
        return 1;
    }
}
