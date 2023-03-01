package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.util.AABB;
import com.github.industrialcraft.blockbyteserver.util.Position;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Entity {
    private final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    public final int clientId;
    public final UUID id;
    protected Position position;
    protected Chunk chunk;
    private boolean removed;
    public Entity(Position position, World world) {
        this.clientId = ID_GENERATOR.incrementAndGet();
        this.id = UUID.randomUUID();
        this.position = position;
        this.chunk = world.getChunk(position.toBlockPos().toChunkPos());
        if(this.chunk == null)
            throw new IllegalStateException("Attempting to spawn entity to unloaded chunk");
        this.chunk.addEntity(this);
        this.removed = false;
    }
    public void tick(){}
    public AABB getBoundingBox(){
        return null;
    }
    public void teleport(Position position, World world){
        Chunk newChunk = world.getChunk(position.toBlockPos().toChunkPos());
        if(newChunk == null)
            throw new IllegalArgumentException("Attempting to teleport entity to unloaded chunk");
        this.position = position;
        if(this.chunk != newChunk){
            this.chunk.removeEntity(this);
            newChunk.addEntity(this);
            this.chunk = newChunk;
        }
    }
    public void teleport(Position position){
        teleport(position, chunk.parent);
    }
    public Position getPosition() {
        return position;
    }
    public Chunk getChunk() {
        return chunk;
    }
    public boolean isRemoved() {
        return removed;
    }
    public void remove(){
        this.removed = true;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
