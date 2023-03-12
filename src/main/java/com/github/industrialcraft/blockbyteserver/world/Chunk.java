package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.Block;
import com.github.industrialcraft.blockbyteserver.content.BlockInstance;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.identifier.Identifier;
import com.google.common.collect.Sets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;

public class Chunk {
    public final World parent;
    public final ChunkPosition position;
    private BlockInstance[] blocks;
    private HashSet<Entity> entities;
    private HashSet<Entity> toAdd;
    private HashSet<PlayerEntity> viewers;
    public Chunk(World parent, ChunkPosition position) {
        this.parent = parent;
        this.position = position;
        this.entities = new HashSet<>();
        this.viewers = new HashSet<>();
        this.toAdd = new HashSet<>();
        this.blocks = new BlockInstance[16*16*16];
        this.parent.chunkGenerator.generateChunk(this.blocks, position, parent, this);
    }
    public Set<Entity> getEntities(){
        return Collections.unmodifiableSet(entities);
    }
    public void tick(){
        this.entities.removeIf(entity -> entity.isRemoved() || entity.chunk != this);
        entities.addAll(toAdd);
        toAdd.clear();
        this.entities.forEach(Entity::tick);
    }
    public void setBlock(Block block, int x, int y, int z){
        checkOffset(x, y, z);
        this.blocks[x+(y*16)+z*(16*16)] = block.createBlockInstance(this, x, y, z);
        for(PlayerEntity viewer : viewers){
            viewer.send(new MessageS2C.SetBlock((position.x()*16)+x, (position.y()*16)+y, (position.z()*16)+z, block.getClientId()));
        }
    }
    public BlockInstance getBlock(int x, int y, int z){
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
            if(playerEntity != player)
                playerEntity.send(message);
        });
    }
    public void addEntity(Entity entity){
        this.toAdd.add(entity);
        announceToViewersExcept(new MessageS2C.AddEntity(entity.getClientType(), entity.clientId, entity.position.x(), entity.position.y(), entity.position.z(), entity.rotation), (entity instanceof PlayerEntity player)?player:null);
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
                        stream.writeInt(getBlock(x, y, z).parent.getClientId());
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
        this.entities.forEach(entity -> player.send(new MessageS2C.AddEntity(entity.getClientType(), entity.clientId, entity.position.x(), entity.position.y(), entity.position.z(), entity.rotation)));
    }
    public void removeViewer(PlayerEntity player){
        this.viewers.remove(player);
        player.send(new MessageS2C.UnloadChunk(position.x(), position.y(), position.z()));
        this.entities.forEach(entity -> player.send(new MessageS2C.DeleteEntity(entity.clientId)));
    }
}
