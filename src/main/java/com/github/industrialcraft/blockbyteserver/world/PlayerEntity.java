package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.net.MessageC2S;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.AABB;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.google.common.collect.Sets;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerEntity extends Entity{
    public final WebSocket socket;
    private final ConcurrentLinkedQueue<MessageC2S> messages;
    private boolean shifting;
    public PlayerEntity(Position position, World world, WebSocket socket) {
        super(position, world);
        this.socket = socket;
        this.messages = new ConcurrentLinkedQueue<>();
        HashSet<ChunkPosition> currentLoadingChunks = getLoadingChunks(position.toBlockPos().toChunkPos());
        for (ChunkPosition chunkPosition : currentLoadingChunks) {
            world.getOrLoadChunk(chunkPosition).addViewer(this);
        }
        this.shifting = false;
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
            if(message instanceof MessageC2S.LeftClickBlock leftClickBlock){
                System.out.println("destroy");
                BlockPosition blockPosition = new BlockPosition(leftClickBlock.x, leftClickBlock.y, leftClickBlock.z);
                chunk.parent.setBlock(blockPosition, Block.AIR);
            }
            if(message instanceof MessageC2S.RightClickBlock rightClickBlock){
                System.out.println("place");
                BlockPosition blockPosition = new BlockPosition(rightClickBlock.x + rightClickBlock.face.xOffset, rightClickBlock.y + rightClickBlock.face.yOffset, rightClickBlock.z + rightClickBlock.face.zOffset);
                for (Entity entity : chunk.getEntities()) {
                    if(entity.getBoundingBox().getCollisionsOnGrid().contains(blockPosition))
                        return;
                }
                if(chunk.parent.getBlock(blockPosition) == Block.AIR){
                    chunk.parent.setBlock(blockPosition, Block.COBBLE);
                }
            }
            message = this.messages.poll();
        }
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
        HashSet<ChunkPosition> loadedPosition = new HashSet<>();
        for(int x = -5;x <= 5;x++){
            for(int y = -5;y <= 5;y++){
                for(int z = -5;z <= 5;z++){
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
