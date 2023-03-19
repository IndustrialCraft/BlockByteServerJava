package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.Block;
import com.github.industrialcraft.blockbyteserver.content.BlockByteItem;
import com.github.industrialcraft.blockbyteserver.content.BlockInstance;
import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerEntity extends Entity{
    public final WebSocket socket;
    private final ConcurrentLinkedQueue<MessageC2S> messages;
    private boolean shifting;
    public final Inventory inventory;
    private int slot;
    private GUI gui;
    public PlayerEntity(Position position, World world, WebSocket socket) {
        super(position, world);
        this.socket = socket;
        this.messages = new ConcurrentLinkedQueue<>();
        HashSet<ChunkPosition> currentLoadingChunks = getLoadingChunks(position.toBlockPos().toChunkPos());
        for (ChunkPosition chunkPosition : currentLoadingChunks) {
            world.getOrLoadChunk(chunkPosition).addViewer(this);
        }
        this.shifting = false;
        this.gui = null;
        this.inventory = new ListeningInventory(9, (inventory1, is) -> {/*todo*/}, this, (slot, item) -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", "hotbar_" + slot);
            json.addProperty("type", "editElement");
            json.addProperty("data_type", "item");
            if(item != null) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("item", ((BlockByteItem) item.getItem()).clientId);
                itemJson.addProperty("count", item.getCount());
                json.add("item", itemJson);
            }
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        });
        for(int i = 0;i < 9;i++){
            JsonObject json = new JsonObject();
            json.addProperty("id", "hotbar_" + i);
            json.addProperty("type", "setElement");
            json.addProperty("element_type", "slot");
            json.addProperty("x", (i * 0.13) - (4.5 * 0.13));
            json.addProperty("y", -0.5);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", "hotbar_0");
            json.addProperty("type", "editElement");
            json.addProperty("data_type", "color");
            json.add("color", MessageS2C.GUIData.createFloatArray(1, 0, 0, 1));
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        {
            JsonObject json = new JsonObject();
            json.addProperty("type", "setElement");
            json.addProperty("id", "cursor");
            json.addProperty("element_type", "image");
            json.addProperty("texture", "cursor");
            json.addProperty("w", 0.05);
            json.addProperty("h", 0.05);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        {
            JsonObject json = new JsonObject();
            json.addProperty("type", "setCursorLock");
            json.addProperty("lock", true);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","cobble")), 3));
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","crusher")), 3));
    }
    public void setGui(GUI newGui){
        if(this.gui != null)
            this.gui.close();
        if(newGui != null) {
            newGui.onOpen();
        }
        {
            JsonObject json = new JsonObject();
            json.addProperty("type", "setCursorLock");
            json.addProperty("lock", newGui == null);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        this.gui = newGui;
    }
    public int getSlot() {
        return slot;
    }
    public boolean isShifting() {
        return shifting;
    }
    @Override
    public void tick() {
        MessageC2S message = this.messages.poll();
        while(message != null){
            if(message instanceof MessageC2S.PlayerPosition playerPosition){
                teleport(new Position(playerPosition.x, playerPosition.y, playerPosition.z));
                this.shifting = playerPosition.shifting;
                this.rotation = playerPosition.rotation;
            }
            if(message instanceof MessageC2S.BreakBlock breakBlock){
                BlockPosition blockPosition = new BlockPosition(breakBlock.x, breakBlock.y, breakBlock.z);
                BlockInstance previousBlock = chunk.parent.getBlock(blockPosition);
                if(previousBlock.parent != Block.AIR) {
                    if(previousBlock.parent.lootTable != null)
                        previousBlock.parent.lootTable.addToInventory(this.inventory, chunk.parent.itemRegistry);
                    chunk.parent.setBlock(blockPosition, Block.AIR);
                }
            }
            if(message instanceof MessageC2S.RightClickBlock rightClickBlock){
                boolean placeCancelled = false;
                if(!isShifting()){
                    BlockPosition rightClickedPosition = new BlockPosition(rightClickBlock.x, rightClickBlock.y, rightClickBlock.z);
                    BlockInstance rightClicked = chunk.parent.getBlock(rightClickedPosition);
                    placeCancelled = rightClicked.parent.onRightClick(chunk.parent, rightClickedPosition, rightClicked, this);
                }
                if(!placeCancelled) {
                    BlockPosition blockPosition = new BlockPosition(rightClickBlock.x + rightClickBlock.face.xOffset, rightClickBlock.y + rightClickBlock.face.yOffset, rightClickBlock.z + rightClickBlock.face.zOffset);
                    for (Entity entity : chunk.getEntities()) {
                        AABB boundingBox = entity.getBoundingBox();
                        if (boundingBox != null && boundingBox.getCollisionsOnGrid().contains(blockPosition))
                            return;
                    }
                    BlockInstance previousBlock = chunk.parent.getBlock(blockPosition);
                    ItemStack hand = getItemInHand();
                    if(hand != null) {
                        BlockByteItem item = (BlockByteItem) hand.getItem();
                        if (previousBlock.parent == Block.AIR && item.place != null) {
                            chunk.parent.setBlock(blockPosition, chunk.parent.blockRegistry.getBlock(item.place));
                            hand.removeCount(1);
                            updateHand();
                        }
                    }
                }
            }
            if(message instanceof MessageC2S.MouseScroll mouseScroll){
                setSlot((((getSlot()-mouseScroll.y)%9)+9)%9);
            }
            if(message instanceof MessageC2S.Keyboard keyboard){
                if(keyboard.down && !keyboard.repeat) {
                    int slot = keyboard.key - 49;
                    if (slot >= 0 && slot <= 8) {
                        setSlot(slot);
                    }
                }
            }
            if(message instanceof MessageC2S.GUIClick guiClick){
                if(this.gui != null)
                    this.gui.onClick(guiClick.id, guiClick.button);
            }
            if(message instanceof MessageC2S.GUIClose guiClose){
                this.setGui(null);
            }
            if(message instanceof MessageC2S.BreakBlockTimeRequest breakBlockTimeRequest){
                send(new MessageS2C.BlockBreakTimeResponse(breakBlockTimeRequest.id, 0.3f));
            }
            message = this.messages.poll();
        }
        if(this.gui != null){
            if(!this.gui.tick())
                setGui(null);
        }
    }
    public void setSlot(int slot) {//todo: clamp
        if(this.slot == slot)
            return;
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", "hotbar_" + this.slot);
            json.addProperty("type", "editElement");
            json.addProperty("data_type", "color");
            json.add("color", MessageS2C.GUIData.createFloatArray(1, 1, 1, 1));
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        this.slot = slot;
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", "hotbar_" + slot);
            json.addProperty("type", "editElement");
            json.addProperty("data_type", "color");
            json.add("color", MessageS2C.GUIData.createFloatArray(1, 0, 0, 1));
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
    }
    public ItemStack getItemInHand(){
        return inventory.getAt(slot);
    }
    public void updateHand(){
        ((ListeningInventory)inventory).updateSlot(slot);
    }
    @Override
    public AABB getBoundingBox() {
        return new AABB(position.x() - 0.3f, position.y(), position.z() - 0.3f, 0.6f, 1.75f-(shifting?0.5f:0f), 0.6f);
    }

    @Override
    public void teleport(Position position, World world) {
        Chunk newChunk = world.getOrLoadChunk(position.toBlockPos().toChunkPos());
        this.position = position;
        if(this.chunk != newChunk){
            HashSet<ChunkPosition> previousLoadingChunks = getLoadingChunks(chunk.position);
            HashSet<ChunkPosition> currentLoadingChunks = getLoadingChunks(newChunk.position);
            this.chunk.transferEntity(this, newChunk);
            if(this.chunk.parent == world) {
                for (ChunkPosition chunkPosition : Sets.difference(previousLoadingChunks, currentLoadingChunks)) {
                    world.getChunk(chunkPosition).removeViewer(this);
                }
                for(ChunkPosition chunkPosition : Sets.difference(currentLoadingChunks, previousLoadingChunks)) {
                    world.getOrLoadChunk(chunkPosition).addViewer(this);
                }
            } else {
                for (ChunkPosition chunkPosition : previousLoadingChunks) {
                    this.chunk.parent.getChunk(chunkPosition).removeViewer(this);
                }
                for (ChunkPosition chunkPosition : currentLoadingChunks) {
                    world.getOrLoadChunk(chunkPosition).addViewer(this);
                }
            }
            this.chunk = newChunk;
        } else {
            this.chunk.announceToViewersExcept(new MessageS2C.MoveEntity(clientId, position.x(), position.y(), position.z(), rotation), this);
        }
    }

    public void onMessage(byte[] data) throws IOException {
        this.messages.add(MessageC2S.fromBytes(data));
    }
    public void send(MessageS2C message){
        try {
            socket.send(message.toBytes());
        } catch (Exception e){
            System.out.println("closed");
        }
    }
    public HashSet<ChunkPosition> getLoadingChunks(ChunkPosition chunkPosition){
        int renderDistance = 5;
        HashSet<ChunkPosition> loadedPosition = new HashSet<>();
        for(int x = -renderDistance;x <= renderDistance;x++){
            for(int y = -renderDistance;y <= renderDistance;y++){
                for(int z = -renderDistance;z <= renderDistance;z++){
                    loadedPosition.add(new ChunkPosition(chunkPosition.x() + x, chunkPosition.y() + y, chunkPosition.z() + z));
                }
            }
        }
        return loadedPosition;
    }
    @Override
    public void remove() {
        throw new IllegalStateException("cannot remove player");
    }

    @Override
    public int getClientType() {
        return 0;
    }

    @Override
    public boolean isRemoved() {
        if(!this.socket.isOpen())
            System.out.println("disconnected");
        return !this.socket.isOpen();
    }
}
