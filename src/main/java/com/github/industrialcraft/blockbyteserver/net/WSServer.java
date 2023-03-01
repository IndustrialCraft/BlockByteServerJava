package com.github.industrialcraft.blockbyteserver.net;

import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class WSServer extends WebSocketServer {
    private final Consumer<WebSocket> openListener;
    public WSServer(int port, Consumer<WebSocket> openListener) {
        super(new InetSocketAddress(port));
        this.openListener = openListener;
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.openListener.accept(conn);
    }
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        try {
            conn.<PlayerEntity>getAttachment().onMessage(message.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {}
    @Override
    public void onStart() {}
    @Override
    public void onMessage(WebSocket conn, String message) {}
}
