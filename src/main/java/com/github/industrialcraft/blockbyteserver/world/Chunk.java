package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class Chunk {
    public final World parent;
    public final ChunkPosition position;
    private Block[] blocks;
    private HashSet<Entity> entities;
    private HashSet<PlayerEntity> viewers;
    public Chunk(World parent, ChunkPosition position) {
        this.parent = parent;
        this.position = position;
        this.entities = new HashSet<>();
        this.viewers = new HashSet<>();
        this.blocks = new Block[16*16*16];
        for(int i = 0;i < 16*16*16;i++)
            this.blocks[i] = Block.AIR;
        for(int x = 0;x < 16;x++){
            for(int z = 0;z < 16;z++){
                setBlock(Block.GRASS, x, 0, z);
            }
        }
    }
    public Set<Entity> getEntities(){
        return Collections.unmodifiableSet(entities);
    }
    public void tick(){
        this.entities.removeIf(Entity::isRemoved);
        this.entities.forEach(Entity::tick);
    }
    public void setBlock(Block block, int x, int y, int z){
        checkOffset(x, y, z);
        this.blocks[x+(y*16)+z*(16*16)] = block;
        for(PlayerEntity viewer : viewers){
            viewer.send(new MessageS2C.SetBlock((position.x()*16)+x, (position.y()*16)+y, (position.z()*16)+z, block.getClientId()));
        }
    }
    public Block getBlock(int x, int y, int z){
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
    public void addEntity(Entity entity){
        this.entities.add(entity);
    }
    public void removeEntity(Entity entity){
        this.entities.remove(entity);
    }
    public void addViewer(PlayerEntity player){
        this.viewers.add(player);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16*16*16*4);
        DataOutputStream stream = new DataOutputStream(byteStream);
        for(int i = 0;i < 16*16*16;i++) {
            try {
                stream.writeInt(this.blocks[i].getClientId());
            } catch (IOException e) {
                throw new RuntimeException(e);
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
    }
    public void removeViewer(PlayerEntity player){
        this.viewers.remove(player);
        player.send(new MessageS2C.UnloadChunk(position.x(), position.y(), position.z()));
    }
}
