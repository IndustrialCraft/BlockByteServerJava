package com.github.industrialcraft.blockbyteserver.net;

import com.github.industrialcraft.blockbyteserver.util.Face;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MessageC2S {
    public static class LeftClickBlock extends MessageC2S{
        public final int x;
        public final int y;
        public final int z;
        public LeftClickBlock(DataInputStream stream) throws IOException {
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();
        }
    }
    public static class RightClickBlock extends MessageC2S{
        public final int x;
        public final int y;
        public final int z;
        public final Face face;
        public final boolean shifting;
        public RightClickBlock(DataInputStream stream) throws IOException {
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();
            this.face = Face.fromId(stream.readByte());
            this.shifting = stream.readBoolean();
        }
    }
    public static class PlayerPosition extends MessageC2S{
        public final float x;
        public final float y;
        public final float z;
        public final boolean shifting;
        public final float rotation;
        public PlayerPosition(DataInputStream stream) throws IOException {
            this.x = stream.readFloat();
            this.y = stream.readFloat();
            this.z = stream.readFloat();
            this.shifting = stream.readBoolean();
            this.rotation = stream.readFloat();
        }
    }
    public static class SelectSlot extends MessageC2S{
        public final int slot;
        public SelectSlot(DataInputStream stream) throws IOException {
            this.slot = stream.readByte();
        }
    }
    public static MessageC2S fromBytes(byte[] data) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(byteStream);
        switch(stream.readByte()){
            case 0:
                return new LeftClickBlock(stream);
            case 1:
                return new RightClickBlock(stream);
            case 2:
                return new PlayerPosition(stream);
            case 3:
                return new SelectSlot(stream);
            default:
                return null;
        }
    }
}
