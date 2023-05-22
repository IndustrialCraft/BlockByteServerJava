package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.custom.BucketItem;
import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import mikera.vectorz.Vector2;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerEntity extends Entity implements IHealthEntity{
    public final WebSocket socket;
    private final ConcurrentLinkedQueue<MessageC2S> messages;
    private boolean shifting;
    public final Inventory inventory;
    private int slot;
    private GUI gui;
    private float health;
    private float actionbarTimer;
    private ItemStack lastHandItem;
    private boolean lastPlayerMoving;
    public final PlayerAbilityStorage abilityStorage;
    public PlayerEntity(Position position, World world, WebSocket socket) {
        super(position, world);
        this.socket = socket;
        this.messages = new ConcurrentLinkedQueue<>();
        HashSet<ChunkPosition> currentLoadingChunks = getLoadingChunks(position.toBlockPos().toChunkPos());
        for (ChunkPosition chunkPosition : currentLoadingChunks) {
            world.getOrLoadChunk(chunkPosition).addViewer(this);
        }
        this.abilityStorage = new PlayerAbilityStorage(this);
        this.shifting = false;
        this.gui = null;
        this.inventory = new ListeningInventory(9, (inventory1, is) -> {
            new ItemEntity(getPosition(), chunk.parent, is);
        }, this, (slot, item) -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", "hotbar_" + slot);
            json.addProperty("type", "editElement");
            json.addProperty("data_type", "item");
            if(item != null) {
                JsonObject itemJson = new JsonObject();
                BlockByteItem blockByteItem = (BlockByteItem) item.getItem();
                itemJson.addProperty("item", blockByteItem.clientId);
                itemJson.addProperty("count", item.getCount());
                BlockByteItem.BarData bar = blockByteItem.getBarData(item);
                if(bar != null) {
                    JsonObject jsonBar = new JsonObject();
                    jsonBar.add("color", MessageS2C.GUIData.createFloatArray(bar.color().r(), bar.color().g(), bar.color().b()));
                    jsonBar.addProperty("progress", bar.progress());
                    itemJson.add("bar", jsonBar);
                }
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
            json.addProperty("id", "health_background");
            json.addProperty("element_type", "image");
            json.addProperty("texture", "health_bar_background");
            json.addProperty("w", 0.50);
            json.addProperty("h", 0.04);
            json.addProperty("x", - (4.5 * 0.13));
            json.addProperty("y", -0.36);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        {
            JsonObject json = new JsonObject();
            json.addProperty("type", "setElement");
            json.addProperty("id", "health");
            json.addProperty("element_type", "image");
            json.addProperty("texture", "health_bar");
            json.addProperty("w", 0.50);
            json.addProperty("h", 0.04);
            json.addProperty("x", - (4.5 * 0.13));
            json.addProperty("y", -0.36);
            json.addProperty("z", 1);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        this.health = getMaxHealth();
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
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", "actionbar");
            json.addProperty("type", "setElement");
            json.addProperty("element_type", "text");
            json.addProperty("x", 0);setActionBar("ahgfdgiufd");
            json.addProperty("y", -0.3);
            json.addProperty("center", true);
            PlayerEntity.this.send(new MessageS2C.GUIData(json));
        }
        //this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","stoneaxe")), 1));
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","stoneshovel")), 1));
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","stoneaxe")), 1));
        //this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","sharpstone")), 1));
        //this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","claybucket")), 1));
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","claybucket")), 1));
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","wet_mud_brick")), 50));
        this.inventory.addItem(new ItemStack(world.itemRegistry.getItem(Identifier.of("bb","grass_fiber")), 20));
        this.actionbarTimer = -1;
        this.lastHandItem = null;
        this.lastPlayerMoving = false;
    }
    public void setActionBar(String message){
        JsonObject json = new JsonObject();
        json.addProperty("id", "actionbar");
        json.addProperty("type", "editElement");
        json.addProperty("data_type", "text");
        json.addProperty("text", message);
        PlayerEntity.this.send(new MessageS2C.GUIData(json));
        this.actionbarTimer = 40;
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
    public Identifier getIdentifier() {
        return Identifier.of("bb", "player");
    }

    @Override
    public void onLeftClick(PlayerEntity player) {
        damage(5);
        Vector2 vec = new Vector2(position.x()-player.position.x(), position.z()-player.position.z());
        vec.normalise();
        vec.multiply(0.3);
        send(new MessageS2C.Knockback((float) vec.x, 0.2f, (float) vec.y, false));
    }

    @Override
    public void tick() {
        MessageC2S message = this.messages.poll();
        while(message != null){
            if(message instanceof MessageC2S.PlayerPosition playerPosition){
                teleport(new Position(playerPosition.x, playerPosition.y, playerPosition.z));
                this.shifting = playerPosition.shifting;
                this.rotation = playerPosition.rotation;
                if(this.lastPlayerMoving != playerPosition.moved){
                    animationController.setAnimation(playerPosition.moved?"walk":"idle");
                    this.lastPlayerMoving = playerPosition.moved;
                }
            }
            if(message instanceof MessageC2S.BreakBlock breakBlock){
                breakAsPlayer(new BlockPosition(breakBlock.x, breakBlock.y, breakBlock.z));
            }
            if(message instanceof MessageC2S.RightClickBlock rightClickBlock){
                boolean placeCancelled = false;
                BlockPosition rightClickedPosition = new BlockPosition(rightClickBlock.x, rightClickBlock.y, rightClickBlock.z);
                AbstractBlockInstance rightClicked = chunk.parent.getBlock(rightClickedPosition);
                if(getItemInHand() != null)
                    placeCancelled = ((BlockByteItem)getItemInHand().getItem()).onRightClickBlock(getItemInHand(), this, rightClickedPosition, rightClicked, rightClickBlock.shifting);
                if((!placeCancelled) && (!isShifting())){
                    placeCancelled = rightClicked.parent.onRightClick(chunk.parent, rightClickedPosition, rightClicked, this);
                }
                if(!placeCancelled) {
                    BlockPosition blockPosition = new BlockPosition(rightClickBlock.x + rightClickBlock.face.xOffset, rightClickBlock.y + rightClickBlock.face.yOffset, rightClickBlock.z + rightClickBlock.face.zOffset);
                    for (Entity entity : chunk.getEntities()) {
                        AABB boundingBox = entity.getBoundingBox();
                        if (boundingBox != null && boundingBox.getCollisionsOnGrid(getPosition().x(), getPosition().y(), getPosition().z()).contains(blockPosition))
                            return;
                    }
                    AbstractBlockInstance previousBlock = chunk.parent.getBlock(blockPosition);
                    ItemStack hand = getItemInHand();
                    if(hand != null) {
                        BlockByteItem item = (BlockByteItem) hand.getItem();
                        if (previousBlock.parent == SimpleBlock.AIR && item.place != null) {
                            AbstractBlock blockToPlace = chunk.parent.blockRegistry.getBlock(item.place);
                            if(blockToPlace.canPlace(this, blockPosition.x(), blockPosition.y(), blockPosition.z(), chunk.parent)) {
                                chunk.parent.setBlock(blockPosition, blockToPlace, new BlockPlacementContext(this, blockPosition, rightClickedPosition, rightClickBlock.face));
                                hand.removeCount(1);
                                updateHand();
                            }
                        }
                    }
                }
            }
            if(message instanceof MessageC2S.RightClick rightClick){
                ItemStack hand = getItemInHand();
                if(hand != null)
                    ((BlockByteItem)hand.getItem()).onRightClick(hand, this, rightClick.shifting);
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
                    if(keyboard.key == 113){//Q
                        ItemStack handItem = getItemInHand();
                        if(handItem != null) {
                            ItemStack toDrop = handItem.clone(1);
                            handItem.removeCount(1);
                            updateHand();
                            float power = 0.7f;
                            new ItemEntity(getPosition().add(0, 1.75f, 0), chunk.parent, toDrop).addVelocity((float) Math.sin(Math.toRadians(rotation)) * power, 0, (float) Math.cos(Math.toRadians(rotation)) * power);
                        }
                    }
                    if(keyboard.key == 101) {//E
                        if (this.gui != null) {
                            this.setGui(null);
                        } else {
                            this.setGui(new PlayerInventoryGUI(this, this.inventory));
                        }
                    }
                }
            }
            if(message instanceof MessageC2S.GUIClick guiClick){
                if(this.gui != null)
                    this.gui.onClick(guiClick.id, guiClick.button, guiClick.shifting);
            }
            if(message instanceof MessageC2S.GuiScroll guiScroll){
                if(this.gui != null)
                    this.gui.onScroll(guiScroll.id, guiScroll.x, guiScroll.y, guiScroll.shifting);
            }
            if(message instanceof MessageC2S.GUIClose guiClose){
                send(new MessageS2C.PlaySound("respawn", this.position, 1, 1, false));
                this.setGui(null);
            }
            if(message instanceof MessageC2S.BreakBlockTimeRequest breakBlockTimeRequest){
                BlockPosition blockPosition = new BlockPosition(breakBlockTimeRequest.x, breakBlockTimeRequest.y, breakBlockTimeRequest.z);
                AbstractBlockInstance blockInstance = chunk.parent.getBlock(blockPosition);
                float time = blockInstance.getBlockBreakingTime(getItemInHand(), this);
                if(time != -1)
                    send(new MessageS2C.BlockBreakTimeResponse(breakBlockTimeRequest.id, time));
            }
            if(message instanceof MessageC2S.LeftClickEntity leftClickEntity){
                Entity entity = chunk.parent.getEntityByClientId(leftClickEntity.id);
                if(entity != null){
                    entity.onLeftClick(this);
                }
            }
            if(message instanceof MessageC2S.RightClickEntity rightClickEntity){
                Entity entity = chunk.parent.getEntityByClientId(rightClickEntity.id);
                if(entity != null){
                    entity.onRightClick(this);
                }
            }
            if(message instanceof MessageC2S.SendMessage sendMessage){
                String msg = sendMessage.message;
                if(msg.startsWith(".speed ")){
                    float speed = Float.parseFloat(msg.replace(".speed ", ""));
                    abilityStorage.setSpeed(speed);
                } else if(msg.startsWith(".movetype ")){
                    PlayerAbilityStorage.EMovementType movementType = null;
                    try{movementType = PlayerAbilityStorage.EMovementType.valueOf(msg.replace(".movetype ", ""));} catch (Exception e){}
                    if(movementType != null)
                        abilityStorage.setMovementType(movementType);
                    else
                        sendChatMessage("unknown move type");
                } else {
                    for (PlayerEntity player : getChunk().parent.getAllPlayers()) {
                        player.sendChatMessage(msg);
                    }
                }
            }
            message = this.messages.poll();
        }
        if(!Objects.equals(lastHandItem, getItemInHand())){
            ItemStack hand = getItemInHand();
            boolean lastBucket = lastHandItem != null && (lastHandItem.getItem() instanceof BucketItem);
            boolean newBucket = hand != null && (hand.getItem() instanceof BucketItem);
            if(lastBucket && !newBucket)
                send(new MessageS2C.FluidSelectable(false));
            if((!lastBucket) && newBucket)
                send(new MessageS2C.FluidSelectable(true));
            if(hand != null) {
                setActionBar(((BlockByteItem)hand.getItem()).itemRenderData.name());
                lastHandItem = hand.clone();
            } else {
                setActionBar("");
                lastHandItem = null;
            }
            //todo: call onEquip on item
        }
        if(actionbarTimer > 0){
            actionbarTimer--;
            if(actionbarTimer == 0){
                JsonObject json = new JsonObject();
                json.addProperty("id", "actionbar");
                json.addProperty("type", "editElement");
                json.addProperty("data_type", "text");
                json.addProperty("text", "");
                PlayerEntity.this.send(new MessageS2C.GUIData(json));
                actionbarTimer = -1;
            }
        }
        if(this.gui != null){
            if(!this.gui.tick())
                setGui(null);
        }
    }
    public void breakAsPlayer(BlockPosition blockPosition){
        AbstractBlockInstance previousBlock = chunk.parent.getBlock(blockPosition);
        if(previousBlock.parent != SimpleBlock.AIR) {
            List<ItemStack> drops = previousBlock.getLoot(this);
            if(drops != null)
                ItemScatterer.scatter(drops, chunk.parent, new Position(blockPosition.x() + 0.25f, blockPosition.y() + 0.25f, blockPosition.z() + 0.25f), 0.25f);
            chunk.parent.setBlock(blockPosition, SimpleBlock.AIR, null);
            previousBlock.onBreak(this, blockPosition.x(), blockPosition.y(), blockPosition.z());
        }
    }
    public void sendChatMessage(String message){
        send(new MessageS2C.ChatMessage(message));
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
        return new AABB(0.6f, 1.75f-(shifting?0.5f:0f), 0.6f);
    }

    @Override
    public void teleport(Position position, World world) {
        Chunk newChunk = world.getOrLoadChunk(position.toBlockPos().toChunkPos());
        this.position = position;
        if(this.chunk != newChunk){
            HashSet<ChunkPosition> previousLoadingChunks = getLoadingChunks(chunk.position);
            HashSet<ChunkPosition> currentLoadingChunks = getLoadingChunks(newChunk.position);//fixme: world change
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
            System.out.println("tried to send " + message + " to disconnected player");
        }
    }
    public HashSet<ChunkPosition> getLoadingChunks(ChunkPosition chunkPosition){
        int renderDistance = 8;
        int verticalRenderDistance = 8;
        HashSet<ChunkPosition> loadedPosition = new HashSet<>();
        for(int x = -renderDistance;x <= renderDistance;x++){
            for(int y = -verticalRenderDistance;y <= verticalRenderDistance;y++){
                for(int z = -renderDistance;z <= renderDistance;z++){
                    loadedPosition.add(new ChunkPosition(chunkPosition.x() + x, chunkPosition.y() + y, chunkPosition.z() + z));
                }
            }
        }
        return loadedPosition;
    }

    public void setHealth(float health){
        health = Math.min(Math.max(health, 0), getMaxHealth());
        float healthbar = health/getMaxHealth();
        {
            JsonObject json = new JsonObject();
            json.addProperty("type", "editElement");
            json.addProperty("id", "health");
            json.addProperty("data_type", "slice");
            json.add("slice", MessageS2C.GUIData.createFloatArray(0, 0, healthbar, 1));
            send(new MessageS2C.GUIData(json));
        }
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", "health");
            json.addProperty("type", "editElement");
            json.addProperty("data_type", "dimension");
            json.add("dimension", MessageS2C.GUIData.createFloatArray(healthbar * 0.5f, 0.04f));
            send(new MessageS2C.GUIData(json));
        }
        this.health = health;
    }
    @Override
    public float getHealth() {
        return health;
    }
    @Override
    public void damage(float amount) {
        setHealth(getHealth()-amount);
    }
    @Override
    public void heal(float amount) {
        setHealth(getHealth()+amount);
    }
    @Override
    public float getMaxHealth() {
        return 100;
    }

    @Override
    public void remove() {
        throw new IllegalStateException("cannot remove player");
    }

    @Override
    public boolean isRemoved() {
        return !this.socket.isOpen();
    }
}
