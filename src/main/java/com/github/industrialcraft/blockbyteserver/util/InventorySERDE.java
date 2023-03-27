package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.content.ItemRegistry;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.InventoryContent;
import com.github.industrialcraft.inventorysystem.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InventorySERDE {
    public static void serialize(DataOutputStream stream, InventoryContent inventory) throws IOException {
        stream.writeInt(inventory.stacks.length);
        for (ItemStack stack : inventory.stacks) {
            if(stack == null){
                stream.writeInt(0);
                continue;
            }
            stream.writeInt(stack.getCount());
            stream.writeUTF(((BlockByteItem)stack.getItem()).id.toString());
            //todo: serialize item data
        }
    }
    public static InventoryContent deserialize(DataInputStream stream, ItemRegistry itemRegistry) throws IOException {
        int size = stream.readInt();
        InventoryContent inventory = new InventoryContent(new ItemStack[size]);
        for(int i = 0;i < size;i++){
            int count = stream.readInt();
            if(count == 0) {
                inventory.stacks[i] = null;
            } else {
                inventory.stacks[i] = new ItemStack(itemRegistry.getItem(Identifier.parse(stream.readUTF())), count);
                //todo: deserialize item data
            }
        }
        return inventory;
    }
}
