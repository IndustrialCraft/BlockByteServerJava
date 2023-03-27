package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.google.gson.JsonObject;

public abstract class GUI {
    public final PlayerEntity player;
    private boolean closed;
    public GUI(PlayerEntity player) {
        this.player = player;
        this.closed = false;
    }
    public abstract void onOpen();
    public abstract boolean tick();
    public abstract void onClick(String id, MessageC2S.GUIClick.EMouseButton button, boolean shifting);
    public abstract void onScroll(String id, int x, int y, boolean shifting);
    public void close(){
        if(!closed){
            onClose();
            closed = true;
        }
    }
    public void onClose(){
        JsonObject json = new JsonObject();
        json.addProperty("type", "removeContainer");
        json.addProperty("container", "gui");
        player.send(new MessageS2C.GUIData(json));
    }
}
