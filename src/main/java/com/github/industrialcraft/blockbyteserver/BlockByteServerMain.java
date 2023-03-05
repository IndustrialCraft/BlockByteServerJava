package com.github.industrialcraft.blockbyteserver;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.net.WSServer;
import com.github.industrialcraft.blockbyteserver.util.ChunkPosition;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import org.java_websocket.WebSocket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockByteServerMain {
    public static void main(String[] args) {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.loadDirectory(new File("data/blocks"));
        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.loadDirectory(new File("data/items"));
        World world = new World(blockRegistry, itemRegistry);
        ConcurrentLinkedQueue<WebSocket> sockets = new ConcurrentLinkedQueue<>();
        new WSServer(4321, sockets::add).start();
        long livingTicks = 0;
        long startTime = System.currentTimeMillis();
        while (true){
            WebSocket connection = sockets.poll();
            if(connection != null){
                ArrayList<MessageS2C.InitializeContent.BlockRenderData> renderData = new ArrayList<>();
                List<Block> blocks = blockRegistry.getBlocks();
                blocks.sort(Comparator.comparingInt(Block::getClientId));
                blocks.forEach(block -> renderData.add(block.renderData));
                ArrayList<MessageS2C.InitializeContent.EntityRenderData> entityRenderData = new ArrayList<>();
                ArrayList<ItemRenderData> itemRenderData = new ArrayList<>();
                List<BlockByteItem> items = itemRegistry.getItems();
                items.sort(Comparator.comparingInt(BlockByteItem::getClientId));
                items.forEach(item -> itemRenderData.add(item.itemRenderData));
                entityRenderData.add(new MessageS2C.InitializeContent.EntityRenderData("player", "player"));
                try {
                    connection.send(new MessageS2C.InitializeContent(renderData, entityRenderData, itemRenderData).toBytes());
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