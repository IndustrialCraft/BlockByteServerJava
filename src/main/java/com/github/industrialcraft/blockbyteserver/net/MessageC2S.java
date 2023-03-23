package com.github.industrialcraft.blockbyteserver.net;

import com.github.industrialcraft.blockbyteserver.util.EFace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageC2S {
    public static class BreakBlock extends MessageC2S{
        public final int x;
        public final int y;
        public final int z;
        public BreakBlock(DataInputStream stream) throws IOException {
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();
        }
    }
    public static class RightClickBlock extends MessageC2S{
        public final int x;
        public final int y;
        public final int z;
        public final EFace face;
        public final boolean shifting;
        public RightClickBlock(DataInputStream stream) throws IOException {
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();
            this.face = EFace.fromId(stream.readByte());
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
    public static class MouseScroll extends MessageC2S{
        public final int x;
        public final int y;
        public MouseScroll(DataInputStream stream) throws IOException {
            this.x = stream.readInt();
            this.y = stream.readInt();
        }
    }
    public static class Keyboard extends MessageC2S{
        public final int key;
        public final boolean down;
        public final boolean repeat;
        public Keyboard(DataInputStream stream) throws IOException {
            this.key = stream.readInt();
            this.down = stream.readBoolean();
            this.repeat = stream.readBoolean();
        }
    }
    public static class GUIClick extends MessageC2S{
        public final String id;
        public final EMouseButton button;
        public GUIClick(DataInputStream stream) throws IOException {
            this.id = readString(stream);
            this.button = EMouseButton.fromId(stream.readByte());
        }
        public enum EMouseButton{
            LEFT(0),
            RIGHT(1);
            public final byte id;
            EMouseButton(int id) {
                this.id = (byte) id;
            }
            public static EMouseButton fromId(byte id){
                for (EMouseButton button : values()) {
                    if(button.id == id)
                        return button;
                }
                return null;
            }
        }
    }
    public static class GUIClose extends MessageC2S{
        public GUIClose(DataInputStream stream){}
    }
    public static class BreakBlockTimeRequest  extends MessageC2S{
        public final int id;
        public final int x;
        public final int y;
        public final int z;
        public BreakBlockTimeRequest(DataInputStream stream) throws IOException {
            this.id = stream.readInt();
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();
        }
    }
    public static class LeftClickEntity  extends MessageC2S{
        public final int id;
        public LeftClickEntity(DataInputStream stream) throws IOException {
            this.id = stream.readInt();
        }
    }
    public static class RightClickEntity  extends MessageC2S{
        public final int id;
        public RightClickEntity(DataInputStream stream) throws IOException {
            this.id = stream.readInt();
        }
    }
    public static MessageC2S fromBytes(byte[] data) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(byteStream);
        switch(stream.readByte()){
            case 0:
                return new BreakBlock(stream);
            case 1:
                return new RightClickBlock(stream);
            case 2:
                return new PlayerPosition(stream);
            case 3:
                return new MouseScroll(stream);
            case 4:
                return new Keyboard(stream);
            case 5:
                return new GUIClick(stream);
            case 6:
                return new GUIClose(stream);
            case 7:
                return new BreakBlockTimeRequest(stream);
            case 8:
                return new LeftClickEntity(stream);
            case 9:
                return new RightClickEntity(stream);
            default:
                return null;
        }
    }
    public static String readString(DataInputStream stream) throws IOException {
        short length = stream.readShort();
        return new String(stream.readNBytes(length), StandardCharsets.UTF_8);
    }
}
