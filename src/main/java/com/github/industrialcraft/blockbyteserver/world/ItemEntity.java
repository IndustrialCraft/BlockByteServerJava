package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class ItemEntity extends Entity{
    public final ItemStack item;
    public ItemEntity(Position position, World world, ItemStack item) {
        super(position, world);
        this.item = item;
    }

    @Override
    public void onSentToPlayer(PlayerEntity player) {
        player.send(new MessageS2C.EntityAddItem(clientId, ((BlockByteItem)item.getItem()).clientId));
    }

    @Override
    public void onLeftClick(PlayerEntity player) {
        if(!isRemoved()){
            player.inventory.addItem(item);
            remove();
        }
    }

    @Override
    public int getClientType() {
        return 1;
    }
}
