package com.github.industrialcraft.blockbyteserver.loot;

import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.content.ItemRegistry;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    public final Identifier item;
    public final int count;
    public LootTable(JsonObject json) {
        this.item = Identifier.parse(json.get("item").getAsString());
        this.count = json.get("count").getAsInt();
    }
    public void addToInventory(Inventory inventory, ItemRegistry itemRegistry){
        inventory.addItem(new ItemStack(itemRegistry.getItem(item), count));
    }
    public List<ItemStack> toItems(ItemRegistry itemRegistry){
        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(itemRegistry.getItem(item), count));
        return items;
    }
}
