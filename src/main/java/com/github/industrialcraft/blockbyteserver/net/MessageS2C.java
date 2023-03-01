package com.github.industrialcraft.blockbyteserver.net;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;

public abstract class MessageS2C {
    public abstract byte[] toBytes() throws IOException;
    public static class SetBlock extends MessageS2C{
        public final int x;
        public final int y;
        public final int z;
        public final int id;
        public SetBlock(int x, int y, int z, int id) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.id = id;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(0);
            stream.writeInt(x);
            stream.writeInt(y);
            stream.writeInt(z);
            stream.writeInt(id);
            return byteStream.toByteArray();
        }
    }
    public static class LoadChunk extends MessageS2C{
        public final int x;
        public final int y;
        public final int z;
        public final byte[] blocks;
        public final int bytesCount;
        public LoadChunk(int x, int y, int z, byte[] blocks, int bytesCount) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blocks = blocks;
            this.bytesCount = bytesCount;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(1);
            stream.writeInt(x);
            stream.writeInt(y);
            stream.writeInt(z);
            stream.writeInt(bytesCount);
            for(int i = 0;i < bytesCount;i++) {
                stream.writeByte(blocks[i]);
            }
            return byteStream.toByteArray();
        }
    }
    public static class UnloadChunk extends MessageS2C{
        public final int x;
        public final int y;
        public final int z;
        public UnloadChunk(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(2);
            stream.writeInt(x);
            stream.writeInt(y);
            stream.writeInt(z);
            return byteStream.toByteArray();
        }
    }
    public static class AddEntity extends MessageS2C{
        public final int entityType;
        public final int id;
        public final float x;
        public final float y;
        public final float z;
        public AddEntity(int entityType, int id, float x, float y, float z) {
            this.entityType = entityType;
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(3);
            stream.writeInt(entityType);
            stream.writeInt(id);
            stream.writeFloat(x);
            stream.writeFloat(y);
            stream.writeFloat(z);
            return byteStream.toByteArray();
        }
    }
    public static class MoveEntity extends MessageS2C{
        public final int id;
        public final float x;
        public final float y;
        public final float z;
        public MoveEntity(int id, float x, float y, float z) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(4);
            stream.writeInt(id);
            stream.writeFloat(x);
            stream.writeFloat(y);
            stream.writeFloat(z);
            return byteStream.toByteArray();
        }
    }
    public static class DeleteEntity extends MessageS2C{
        public final int id;
        public DeleteEntity(int id) {
            this.id = id;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(5);
            stream.writeInt(id);
            return byteStream.toByteArray();
        }
    }
    public static class InitializeBlocks extends MessageS2C{
        public final List<BlockRenderData> blockRenderData;
        public InitializeBlocks(List<BlockRenderData> blockRenderData) {
            this.blockRenderData = blockRenderData;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(6);
            stream.writeShort(blockRenderData.size());
            for (BlockRenderData blockData : blockRenderData) {
                writeString(stream, blockData.north);
                writeString(stream, blockData.south);
                writeString(stream, blockData.up);
                writeString(stream, blockData.down);
                writeString(stream, blockData.left);
                writeString(stream, blockData.right);
            }
            return byteStream.toByteArray();
        }
        private static void writeString(DataOutputStream stream, String value) throws IOException {
            byte[] data = value.getBytes(StandardCharsets.UTF_8);
            stream.writeShort(data.length);
            for (byte ch : data) {
                stream.writeByte(ch);
            }
        }
        public record BlockRenderData(String north, String south, String up, String down, String left, String right){

        }
    }
}
