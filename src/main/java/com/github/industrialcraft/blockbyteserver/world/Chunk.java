package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlock;
import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.util.ITicking;
import com.google.common.collect.Sets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.Deflater;

public class Chunk {
    private static final int UNLOAD_TIME = 20;

    public final World parent;
    public final ChunkPosition position;
    private AbstractBlockInstance[] blocks;
    private HashSet<Entity> entities;
    private HashSet<Entity> toAdd;
    private HashSet<PlayerEntity> viewers;
    private LinkedList<Integer> tickingBlocks;
    private int unloadTimer;
    private boolean populated;
    public Chunk(World parent, ChunkPosition position) {
        this.parent = parent;
        this.position = position;
        this.entities = new HashSet<>();
        this.viewers = new HashSet<>();
        this.toAdd = new HashSet<>();
        this.blocks = new AbstractBlockInstance[16*16*16];
        if(this.parent.worldSERDE.isChunkSaved(parent, position)){
            this.populated = this.parent.worldSERDE.load(this, blocks);
        } else {
            this.parent.chunkGenerator.generateChunk(this.blocks, position, parent, this);
            this.populated = false;
        }
        this.tickingBlocks = new LinkedList<>();
        this.unloadTimer = UNLOAD_TIME;
    }
    public boolean isPopulated() {
        return populated;
    }
    public void load(){
        for(int x = 0;x < 16;x++){
            for(int y = 0;y < 16;y++){
                for(int z = 0;z < 16;z++){
                    int blockOffset = x + (y * 16) + z * (16 * 16);
                    AbstractBlockInstance block = blocks[blockOffset];
                    if(block instanceof ITicking){
                        tickingBlocks.add(blockOffset);
                    }
                    block.postSet(this, x, y, z);
                }
            }
        }
    }
    public AbstractBlockInstance[] getUnsafeBlocks() {
        return blocks;
    }
    public Set<Entity> getEntities(){
        return Collections.unmodifiableSet(entities);
    }
    public void tick(){
        this.entities.removeIf(entity -> {
            if(entity instanceof PlayerEntity player){
                if(entity.isRemoved())
                    for (ChunkPosition loadingChunk : player.getLoadingChunks(player.chunk.position)) {
                        Chunk chunk = parent.getChunk(loadingChunk);
                        chunk.removeViewer(player);
                    }
            }
            return entity.isRemoved() || entity.chunk != this;
        });
        if(!populated){
            if(areAllNeighborChunksLoaded()){
                parent.chunkGenerator.populate(this);
                this.populated = true;
            }
        }
        for (Entity entity : toAdd) {
            announceToViewersExcept(new MessageS2C.AddEntity(entity.getClientType(), entity.clientId, entity.position.x(), entity.position.y(), entity.position.z(), entity.rotation), (entity instanceof PlayerEntity player)?player:null);
            this.viewers.forEach(entity::onSentToPlayer);
        }
        entities.addAll(toAdd);
        toAdd.clear();
        this.entities.forEach(Entity::tick);
        this.tickingBlocks.forEach(offset -> ((ITicking)this.blocks[offset]).tick());
        unloadTimer--;
        if(viewers.size() > 0){
            unloadTimer = UNLOAD_TIME;
        }
    }
    private boolean areAllNeighborChunksLoaded(){
        for(int x = -1;x <= 1;x++){
            for(int y = -1;y <= 1;y++){
                for(int z = -1;z <= 1;z++){
                    if(parent.getChunk(new ChunkPosition(position.x()+x, position.y()+y, position.z()+z)) == null)
                        return false;
                }
            }
        }
        return true;
    }
    public boolean shouldUnload(){
        return unloadTimer<=0 && viewers.size() == 0;
    }
    public void setBlock(AbstractBlock block, int x, int y, int z, Object data){
        checkOffset(x, y, z);
        int blockOffset = x + (y * 16) + z * (16 * 16);
        AbstractBlockInstance instance = this.blocks[blockOffset];
        instance.onDestroy();
        AbstractBlockInstance newInstance = block.createBlockInstance(this, x, y, z, data);
        this.blocks[blockOffset] = newInstance;
        boolean previousTicking = instance instanceof ITicking;
        boolean currentTicking = newInstance instanceof ITicking;
        if(previousTicking && !currentTicking)
            this.tickingBlocks.removeFirstOccurrence(blockOffset);
        if(currentTicking && !previousTicking)
            this.tickingBlocks.add(blockOffset);

        BlockPosition blockPosition = new BlockPosition(x, y, z);
        for(EFace face : EFace.values()){
            parent.getBlock(new BlockPosition((x + (position.x()*16)) + face.xOffset, (y + (position.y()*16)) + face.yOffset, (z + (position.z() * 16)) + face.zOffset)).onNeighborUpdate(blockPosition, instance, newInstance, face.opposite());
        }

        newInstance.postSet(this, x, y, z);

        for(PlayerEntity viewer : viewers){
            viewer.send(new MessageS2C.SetBlock((position.x()*16)+x, (position.y()*16)+y, (position.z()*16)+z, newInstance.getClientId()));
            newInstance.onSentToPlayer(viewer);
        }
    }
    public AbstractBlockInstance getBlock(int x, int y, int z){
        checkOffset(x, y, z);
        return this.blocks[x+(y*16)+z*(16*16)];
    }
    private void checkOffset(int x, int y, int z){
        if(x < 0 || x > 15)
            throw new IllegalArgumentException("x must be in range 0..15, was " + x);
        if(y < 0 || y > 15)
            throw new IllegalArgumentException("y must be in range 0..15, was " + y);
        if(z < 0 || z > 15)
            throw new IllegalArgumentException("z must be in range 0..15, was " + z);
    }
    public void announceToViewersExcept(MessageS2C message, PlayerEntity player){
        this.viewers.forEach(playerEntity -> {
            if(playerEntity != player) {
                playerEntity.send(message);
            }
        });
    }
    public void addEntity(Entity entity){
        this.toAdd.add(entity);
    }
    public void transferEntity(Entity entity, Chunk other){
        if(this == other)
            return;
        other.toAdd.add(entity);
        for (PlayerEntity playerEntity : Sets.difference(this.viewers, other.viewers)) {
            playerEntity.send(new MessageS2C.DeleteEntity(entity.clientId));
        }
        for (PlayerEntity playerEntity : Sets.difference(other.viewers, this.viewers)) {
            playerEntity.send(new MessageS2C.AddEntity(entity.getClientType(), entity.clientId, entity.position.x(), entity.position.y(), entity.position.z(), entity.rotation));
            entity.onSentToPlayer(playerEntity);
        }
    }
    public void addViewer(PlayerEntity player){
        this.viewers.add(player);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16*16*16*4);
        DataOutputStream stream = new DataOutputStream(byteStream);
        for(int x = 0;x < 16;x++){
            for(int y = 0;y < 16;y++){
                for(int z = 0;z < 16;z++){
                    try {
                        AbstractBlockInstance block = getBlock(x, y, z);
                        block.onSentToPlayer(player);
                        stream.writeInt(block.getClientId());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        try {
            byteStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = new byte[16*16*16*4];
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(byteStream.toByteArray());
        deflater.finish();
        int bytesCount = deflater.deflate(bytes);
        player.send(new MessageS2C.LoadChunk(position.x(), position.y(), position.z(), bytes, bytesCount));
        this.entities.forEach(entity -> {
            player.send(new MessageS2C.AddEntity(entity.getClientType(), entity.clientId, entity.position.x(), entity.position.y(), entity.position.z(), entity.rotation));
            entity.onSentToPlayer(player);
        });
    }
    public void removeViewer(PlayerEntity player){
        this.viewers.remove(player);
        if(!player.isRemoved()) {
            player.send(new MessageS2C.UnloadChunk(position.x(), position.y(), position.z()));
            this.entities.forEach(entity -> player.send(new MessageS2C.DeleteEntity(entity.clientId)));
        }
    }
}
