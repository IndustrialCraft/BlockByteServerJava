package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonObject;

import java.util.HashMap;

public abstract class InventoryGUI extends GUI{
    protected HashMap<String,Slot> slots;
    private ItemStack hand;
    public InventoryGUI(PlayerEntity player) {
        super(player);
        this.slots = new HashMap<>();
        for(int i = 0;i < 9;i++)
            this.slots.put("hotbar_" + i, new Slot(player.inventory, i));
        this.hand = null;
    }

    @Override
    public void onOpen() {
        for(var entry : slots.entrySet()){
            if(entry.getValue().eventOnly)
                continue;
            JsonObject json = new JsonObject();
            json.addProperty("id", entry.getKey());
            json.addProperty("type", "setElement");
            json.addProperty("element_type", "slot");
            json.addProperty("x", entry.getValue().x);
            json.addProperty("y", entry.getValue().y);
            player.send(new MessageS2C.GUIData(json));
        }
    }

    @Override
    public void onClick(String id, MessageC2S.GUIClick.EMouseButton button) {
        Slot slot = slots.get(id);
        if(slot != null){
            ItemStack temp = hand;
            setHand(slot.inventory.getAt(slot.slot));
            slot.inventory.setAt(slot.slot, temp);
        }
    }
    @Override
    public boolean tick() {
        for (var entry : this.slots.entrySet()) {
            if(entry.getValue().shouldUpdateAndResyncVersion()){
                var item = entry.getValue().inventory.getAt(entry.getValue().slot);
                JsonObject json = new JsonObject();
                json.addProperty("id", entry.getKey());
                json.addProperty("type", "editElement");
                json.addProperty("data_type", "item");
                if(item != null) {
                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("item", ((BlockByteItem) item.getItem()).clientId);
                    itemJson.addProperty("count", item.getCount());
                    json.add("item", itemJson);
                }
                player.send(new MessageS2C.GUIData(json));
            }
        }
        return onTick();
    }
    public abstract boolean onTick();
    private void setHand(ItemStack hand){
        this.hand = hand;
        JsonObject json = new JsonObject();
        json.addProperty("type", "setElement");
        json.addProperty("id", "cursor");
        if(hand == null){
            json.addProperty("element_type", "image");
            json.addProperty("texture", "cursor");
            json.addProperty("w", 0.05);
            json.addProperty("h", 0.05);
        } else {
            json.addProperty("element_type", "slot");
            json.addProperty("background", false);
            {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("item", ((BlockByteItem)hand.getItem()).clientId);
                itemJson.addProperty("count", hand.getCount());
                json.add("item", itemJson);
            }
        }
        player.send(new MessageS2C.GUIData(json));
    }
    @Override
    public void onClose() {
        super.onClose();
        //todo: drop hand
        setHand(null);
    }
    public static class Slot{
        public final Inventory inventory;
        public final int slot;
        public final float x;
        public final float y;
        public final boolean eventOnly;
        private int lastUpdate;
        public Slot(Inventory inventory, int slot, float x, float y) {
            this.inventory = inventory;
            this.slot = slot;
            this.x = x;
            this.y = y;
            this.lastUpdate = -1;
            this.eventOnly = false;
        }
        public Slot(Inventory inventory, int slot) {
            this.inventory = inventory;
            this.slot = slot;
            this.x = 0;
            this.y = 0;
            this.lastUpdate = -1;
            this.eventOnly = true;
        }
        public boolean shouldUpdateAndResyncVersion(){
            if(inventory instanceof IInventoryWithSlotVersioning inventoryWithSlotVersioning){
                int version = inventoryWithSlotVersioning.getVersion(slot);
                if(version != lastUpdate){
                    this.lastUpdate = version;
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
    }
}
