package com.github.industrialcraft.blockbyteserver;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.net.WSServer;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockByteServerMain {
    public static void main(String[] args) {
        World world = new World();
        ConcurrentLinkedQueue<WebSocket> sockets = new ConcurrentLinkedQueue<>();
        //todo: add to queue as this gets executed async
        new WSServer(4321, sockets::add).start();
        long livingTicks = 0;
        long startTime = System.currentTimeMillis();
        while (true){
            WebSocket connection = sockets.poll();
            if(connection != null){
                ArrayList<MessageS2C.InitializeBlocks.BlockRenderData> renderData = new ArrayList<>();
                renderData.add(new MessageS2C.InitializeBlocks.BlockRenderData("grass_side", "grass_side", "grass", "dirt", "grass_side", "grass_side"));
                renderData.add(new MessageS2C.InitializeBlocks.BlockRenderData("cobble", "cobble", "cobble", "cobble", "cobble", "cobble"));
                try {
                    connection.send(new MessageS2C.InitializeBlocks(renderData).toBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                world.getOrLoadChunk(new ChunkPosition(0, 0, 0));
                PlayerEntity playerEntity = new PlayerEntity(new Position(0, 0, 0), world, connection);
                connection.setAttachment(playerEntity);
            }
            world.tick();
            while(((livingTicks*50)+startTime) > System.currentTimeMillis()){
                Thread.yield();
            }
            livingTicks++;
        }
    }
}