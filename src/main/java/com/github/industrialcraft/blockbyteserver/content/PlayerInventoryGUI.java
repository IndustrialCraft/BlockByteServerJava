package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.world.BasicVersionedInventory;
import com.github.industrialcraft.blockbyteserver.world.InventoryGUI;
import com.github.industrialcraft.blockbyteserver.world.ItemEntity;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Objects;

public class PlayerInventoryGUI extends InventoryGUI {
    public final BasicVersionedInventory craftingGridInventory;
    public final BasicVersionedInventory craftingResultInventory;
    private boolean recheckRecipe;
    public PlayerInventoryGUI(PlayerEntity player, Inventory transferInventory) {
        super(player, transferInventory);
        this.craftingGridInventory = new BasicVersionedInventory(9, (inventory1, is) -> {
            new ItemEntity(player.getPosition(), player.getChunk().parent, is);
        }, null){
            @Override
            public ItemStack setAt(int index, ItemStack itemStack) {
                recheckRecipe = true;
                return super.setAt(index, itemStack);
            }
        };
        this.craftingResultInventory = new BasicVersionedInventory(1, null, null){
            @Override
            public ItemStack setAt(int index, ItemStack itemStack) {
                if(itemStack == null) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack is = craftingGridInventory.getAt(i);
                        if(is != null) {
                            is.removeCount(1);
                            craftingGridInventory.setAt(i, is);
                        }
                    }
                }
                return super.setAt(index, itemStack);
            }
        };
        for(int x = 0;x < 3;x++) {
            for (int y = 0; y < 3; y++) {
                this.slots.put("gui_crafting_" + (x+(y*3)), new Slot(craftingGridInventory, (x+(y*3)), -0.5f+(x*0.1f), -0.25f+((2-y)*0.1f), transferInventory, false));
            }
        }
        this.slots.put("gui_crafting_output", new Slot(craftingResultInventory, 0, 0.5f, 0, transferInventory, true));
    }
    private void tryUpdateRecipe(){
        List<CraftingRecipe> recipes = player.getChunk().parent.recipeRegistry.getRecipesForType(Identifier.of("bb","crafting"));
        for (CraftingRecipe recipe : recipes) {
            boolean incorrect = false;
            for(int i = 0;i < 9;i++){
                Identifier itemId;
                ItemStack item = craftingGridInventory.getAt(i);
                if(item == null){
                    itemId = null;
                } else {
                    itemId = ((BlockByteItem)craftingGridInventory.getAt(i).getItem()).id;
                }
                if(!(Objects.equals(recipe.pattern[i], itemId))){
                    incorrect = true;
                    break;
                }
            }
            if(!incorrect){
                craftingResultInventory.setAt(0, new ItemStack(player.getChunk().parent.itemRegistry.getItem(recipe.output), 1));
                return;
            }
        }
    }
    @Override
    public boolean onTick() {
        if(recheckRecipe){
            recheckRecipe = false;
            tryUpdateRecipe();
        }
        return true;
    }
    @Override
    public void onClose() {
        super.onClose();
        this.craftingGridInventory.dropAll();
    }
    public static class CraftingRecipe extends Recipe{
        public final Identifier[] pattern;
        public final Identifier output;
        public CraftingRecipe(Identifier id, JsonObject json) {
            super(id);
            this.pattern = new Identifier[9];
            JsonArray pattern = json.getAsJsonArray("pattern");
            for(int i = 0;i < 9;i++){
                JsonElement part = pattern.get(i);
                if(part == null || part.isJsonNull())
                    this.pattern[i] = null;
                else
                    this.pattern[i] = Identifier.parse(part.getAsString());
            }
            this.output = Identifier.parse(json.get("output").getAsString());
        }
    }
}
