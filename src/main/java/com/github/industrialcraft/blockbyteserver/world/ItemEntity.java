package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.AABB;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;

public class ItemEntity extends PhysicsEntity{
    public final ItemStack item;
    public ItemEntity(Position position, World world, ItemStack item) {
        super(position, world);
        this.item = item;
    }

    @Override
    public void onSentToPlayer(PlayerEntity player) {
        player.send(new MessageS2C.EntityAddItem(clientId, 0, ((BlockByteItem)item.getItem()).clientId));
    }

    @Override
    public AABB getBoundingBox() {
        return new AABB(0.5f, 0.5f, 0.5f);
    }

    @Override
    public void onLeftClick(PlayerEntity player) {
        if(!isRemoved()){
            player.inventory.addItem(item);
            remove();
        }
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb", "item");
    }
}
