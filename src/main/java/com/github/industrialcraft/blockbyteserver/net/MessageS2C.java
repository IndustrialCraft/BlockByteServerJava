package com.github.industrialcraft.blockbyteserver.net;

import com.github.industrialcraft.blockbyteserver.content.ItemRenderData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        public final float rotation;
        public AddEntity(int entityType, int id, float x, float y, float z, float rotation) {
            this.entityType = entityType;
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotation = rotation;
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
            stream.writeFloat(rotation);
            return byteStream.toByteArray();
        }
    }
    public static class MoveEntity extends MessageS2C{
        public final int id;
        public final float x;
        public final float y;
        public final float z;
        public final float rotation;
        public MoveEntity(int id, float x, float y, float z, float rotation) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotation = rotation;
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
            stream.writeFloat(rotation);
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
    public static class InitializeContent extends MessageS2C{
        public final List<BlockRenderData> blockRenderData;
        public final List<EntityRenderData> entityRenderData;
        public final List<ItemRenderData> itemRenderData;
        public InitializeContent(List<BlockRenderData> blockRenderData, List<EntityRenderData> entityRenderData, List<ItemRenderData> itemRenderData) {
            this.blockRenderData = blockRenderData;
            this.entityRenderData = entityRenderData;
            this.itemRenderData = itemRenderData;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(6);
            stream.writeShort(blockRenderData.size());
            for (BlockRenderData blockData : blockRenderData) {
                writeString(stream, blockData.json.toString());
            }
            stream.writeShort(entityRenderData.size());
            for (EntityRenderData entityData : entityRenderData) {
                writeString(stream, entityData.model);
                writeString(stream, entityData.texture);
            }
            stream.writeShort(itemRenderData.size());
            for (ItemRenderData itemData : itemRenderData) {
                writeString(stream, itemData.name());
                writeString(stream, itemData.texture());
            }
            return byteStream.toByteArray();
        }
        public record BlockRenderData(JsonObject json){

        }
        public record EntityRenderData(String model, String texture){

        }
    }
    public static class GUIData extends MessageS2C{
        public final JsonObject json;
        public GUIData(JsonObject json) {
            this.json = json;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(7);
            writeString(stream, json.toString());
            return byteStream.toByteArray();
        }
        public static JsonArray createColor(float r, float g, float b, float a){
            var col = new JsonArray();
            col.add(r);
            col.add(g);
            col.add(b);
            col.add(a);
            return col;
        }
    }
    private static void writeString(DataOutputStream stream, String value) throws IOException {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        stream.writeShort(data.length);
        for (byte ch : data) {
            stream.writeByte(ch);
        }
    }
}
