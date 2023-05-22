package com.github.industrialcraft.blockbyteserver.net;

import com.github.industrialcraft.blockbyteserver.util.PlayerAbilityStorage;
import com.github.industrialcraft.blockbyteserver.util.Position;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
        public final String animation;
        public final float animationStartTime;
        public AddEntity(int entityType, int id, float x, float y, float z, float rotation, String animation, float animationStartTime) {
            this.entityType = entityType;
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotation = rotation;
            this.animation = animation;
            this.animationStartTime = animationStartTime;
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
            MessageS2C.writeString(stream, animation);
            stream.writeFloat(animationStartTime);
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
    public static class GUIData extends MessageS2C{
        public final JsonObject json;
        public GUIData(JsonObject json) {
            this.json = json;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(6);
            writeString(stream, json.toString());
            return byteStream.toByteArray();
        }
        public static JsonArray createFloatArray(float... vals){
            var col = new JsonArray();
            for (float val : vals) {
                col.add(val);
            }
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
    public static class BlockBreakTimeResponse extends MessageS2C{
        public final int id;
        public final float time;
        public BlockBreakTimeResponse(int id, float time) {
            this.id = id;
            this.time = time;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(7);
            stream.writeInt(id);
            stream.writeFloat(time);
            return byteStream.toByteArray();
        }
    }
    public static class EntityAddItem extends MessageS2C{
        public final int entityId;
        public final int itemIndex;
        public final int itemId;
        public EntityAddItem(int entityId, int itemIndex, int itemId) {
            this.entityId = entityId;
            this.itemIndex = itemIndex;
            this.itemId = itemId;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(8);
            stream.writeInt(entityId);
            stream.writeInt(itemIndex);
            stream.writeInt(itemId);
            return byteStream.toByteArray();
        }
    }
    public static class BlockAddItem extends MessageS2C{
        public final int x;
        public final int y;
        public final int z;
        public final float xOffset;
        public final float yOffset;
        public final float zOffset;
        public final int itemIndex;
        public final int itemId;
        public BlockAddItem(int x, int y, int z, float xOffset, float yOffset, float zOffset, int itemIndex, int itemId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.itemIndex = itemIndex;
            this.itemId = itemId;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(9);
            stream.writeInt(x);
            stream.writeInt(y);
            stream.writeInt(z);
            stream.writeFloat(xOffset);
            stream.writeFloat(yOffset);
            stream.writeFloat(zOffset);
            stream.writeInt(itemIndex);
            stream.writeInt(itemId);
            return byteStream.toByteArray();
        }
    }
    public static class BlockRemoveItem extends MessageS2C{
        public final int x;
        public final int y;
        public final int z;
        public final int itemIndex;
        public BlockRemoveItem(int x, int y, int z, int itemIndex) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.itemIndex = itemIndex;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(10);
            stream.writeInt(x);
            stream.writeInt(y);
            stream.writeInt(z);
            stream.writeInt(itemIndex);
            return byteStream.toByteArray();
        }
    }
    public static class BlockMoveItem extends MessageS2C{
        public final int x;
        public final int y;
        public final int z;
        public final float xOffset;
        public final float yOffset;
        public final float zOffset;
        public final int itemIndex;
        public BlockMoveItem(int x, int y, int z, float xOffset, float yOffset, float zOffset, int itemIndex) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.itemIndex = itemIndex;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(11);
            stream.writeInt(x);
            stream.writeInt(y);
            stream.writeInt(z);
            stream.writeFloat(xOffset);
            stream.writeFloat(yOffset);
            stream.writeFloat(zOffset);
            stream.writeInt(itemIndex);
            return byteStream.toByteArray();
        }
    }
    public static class Knockback extends MessageS2C{
        public final float x;
        public final float y;
        public final float z;
        public final boolean set;
        public Knockback(float x, float y, float z, boolean set) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.set = set;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(12);
            stream.writeFloat(x);
            stream.writeFloat(y);
            stream.writeFloat(z);
            stream.writeBoolean(set);
            return byteStream.toByteArray();
        }
    }
    public static class FluidSelectable extends MessageS2C{
        public final boolean selectable;
        public FluidSelectable(boolean selectable) {
            this.selectable = selectable;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(13);
            stream.writeBoolean(selectable);
            return byteStream.toByteArray();
        }
    }
    public static class PlaySound extends MessageS2C{
        public final String id;
        public final Position position;
        public final float gain;
        public final float pitch;
        public final boolean relative;
        public PlaySound(String id, Position position, float gain, float pitch, boolean relative) {
            this.id = id;
            this.position = position;
            this.gain = gain;
            this.pitch = pitch;
            this.relative = relative;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(14);
            MessageS2C.writeString(stream, id);
            stream.writeFloat(position.x());
            stream.writeFloat(position.y());
            stream.writeFloat(position.z());
            stream.writeFloat(gain);
            stream.writeFloat(pitch);
            stream.writeBoolean(relative);
            return byteStream.toByteArray();
        }
    }
    public static class EntityAnimation extends MessageS2C{
        public final int entityId;
        public final String animation;
        public EntityAnimation(int entityId, String animation) {
            this.entityId = entityId;
            this.animation = animation;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(15);
            stream.writeInt(entityId);
            MessageS2C.writeString(stream, animation);
            return byteStream.toByteArray();
        }
    }
    public static class ChatMessage extends MessageS2C{
        public final String message;
        public ChatMessage(String message) {
            this.message = message;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(16);
            MessageS2C.writeString(stream, message);
            return byteStream.toByteArray();
        }
    }
    public static class PlayerAbilities extends MessageS2C{
        public final float speed;
        public final PlayerAbilityStorage.EMovementType movementType;
        public PlayerAbilities(float speed, PlayerAbilityStorage.EMovementType movementType) {
            this.speed = speed;
            this.movementType = movementType;
        }
        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            stream.writeByte(17);
            stream.writeFloat(speed);
            stream.writeByte(movementType.id);
            return byteStream.toByteArray();
        }
    }
}
