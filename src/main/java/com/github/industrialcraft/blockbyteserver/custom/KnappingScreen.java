package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.Recipe;
import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.world.BasicVersionedInventory;
import com.github.industrialcraft.blockbyteserver.world.InventoryGUI;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class KnappingScreen extends InventoryGUI {
    private final Identifier item;
    private final Inventory inventory;
    private boolean finished;
    private boolean[] knapPattern;
    public KnappingScreen(PlayerEntity player, Inventory transferInventory, Identifier item) {
        super(player, transferInventory);
        this.item = item;
        inventory = new BasicVersionedInventory(1, null, null);
        this.slots.put("gui_output", new Slot(inventory, 0, 0.5f, 0f, transferInventory, true));
        for(int x = 0;x < 5;x++) {
            for (int y = 0; y < 5; y++) {
                JsonObject json = new JsonObject();
                json.addProperty("id", "gui_knapping_" + (x+(y*5)));
                json.addProperty("type", "setElement");
                json.addProperty("element_type", "image");
                json.addProperty("texture", "cobble");
                json.addProperty("y", -0.25+((4-y)*0.1));
                json.addProperty("x", -0.5+(x*0.1));
                json.addProperty("w", 0.1f);
                json.addProperty("h", 0.1f);
                player.send(new MessageS2C.GUIData(json));
            }
        }
        this.finished = false;
        this.knapPattern = new boolean[25];
        for(int i = 0;i < 25;i++)
            knapPattern[i] = true;
    }
    @Override
    public boolean onTick() {
        return !finished;
    }
    @Override
    public void onClick(String id, MessageC2S.GUIClick.EMouseButton button, boolean shifting) {
        if(finished)
            return;
        Slot slot = slots.get(id);
        if(slot != null && inventory.getAt(0) != null){
            player.inventory.addItem(inventory.getAt(0).clone());
            this.finished = true;
        }
        if(id.startsWith("gui_knapping_")){
            int knapSpaceId = Integer.parseInt(id.replace("gui_knapping_", ""));
            if(knapPattern[knapSpaceId]){
                {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", id);
                    json.addProperty("type", "setElement");
                    player.send(new MessageS2C.GUIData(json));
                }
                knapPattern[knapSpaceId] = false;
                var recipes = player.getChunk().parent.recipeRegistry.getRecipesForType(Identifier.of("bb","knapping"));
                for (Recipe e : recipes) {
                    KnappingRecipe recipe = (KnappingRecipe) e;
                    if(recipe.item.equals(item)){
                        boolean incorrect = false;
                        for(int i = 0;i < 25;i++)
                            if(recipe.pattern[i] != knapPattern[i]) {
                                incorrect = true;
                                break;
                            }
                        if(!incorrect){
                            inventory.setAt(0, new ItemStack(player.getChunk().parent.itemRegistry.getItem(recipe.output), 1));
                            break;
                        }
                    }
                }
            }
        }
    }
    public static class KnappingRecipe extends Recipe{
        public final Identifier item;
        public final boolean[] pattern;
        public final Identifier output;
        public KnappingRecipe(Identifier id, JsonObject json) {
            super(id);
            this.item = Identifier.parse(json.get("item").getAsString());
            this.output = Identifier.parse(json.get("output").getAsString());
            JsonArray pattern = json.getAsJsonArray("pattern");
            if(pattern.size() != 25)
                throw new IllegalStateException("invalid knapping recipe " + id);
            this.pattern = new boolean[25];
            for(int i = 0;i < 25;i++){
                this.pattern[i] = pattern.get(i).getAsBoolean();
            }
        }
    }
}
