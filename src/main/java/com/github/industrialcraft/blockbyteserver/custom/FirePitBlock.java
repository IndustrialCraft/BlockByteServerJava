package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.*;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.ItemScatterer;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FirePitBlock extends AbstractBlock {
    public final int clientId;
    public final BlockRegistry.BlockRenderData[] renderData;
    public FirePitBlock(AtomicInteger clientId) {
        this.renderData = new BlockRegistry.BlockRenderData[5];
        for(int i = 0;i < renderData.length;i++){
            JsonObject jsonRenderData = new JsonObject();
            jsonRenderData.addProperty("type", "static");
            jsonRenderData.addProperty("texture", "fire_pit");
            jsonRenderData.addProperty("model", "fire_pit_" + i);
            renderData[i] = new BlockRegistry.BlockRenderData(jsonRenderData);
        }
        this.clientId = clientId.get();
        clientId.addAndGet(5);
    }
    @Override
    public AbstractBlockInstance<FirePitBlock> createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        return new FirePitBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), chunk);
    }
    @Override
    public void registerRenderData(HashMap<Integer, BlockRegistry.BlockRenderData> renderData) {
        for (int i = 0;i < this.renderData.length;i++) {
            renderData.put(clientId+i, this.renderData[i]);
        }
    }

    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        return ((FirePitBlockInstance)instance).onRightClick(player);
    }

    @Override
    public int getDefaultClientId() {
        return this.clientId;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of("bb","conveyor");
    }

    @Override
    public boolean canPlace(PlayerEntity player, int x, int y, int z, World world) {
        return true;
    }
    @Override
    public boolean isNoCollide() {
        return false;
    }
    public static class FirePitBlockInstance extends AbstractBlockInstance<FirePitBlock> implements ITicking {
        public final int x;
        public final int y;
        public final int z;
        private boolean isValid;
        public final Chunk chunk;
        private int logs;
        private ItemStack item;
        private int finishTime;
        public FirePitBlockInstance(FirePitBlock parent, int x, int y, int z, Chunk chunk) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.chunk = chunk;
            this.isValid = true;
            this.logs = 0;
            this.item = null;
            this.finishTime = -1;
        }
        @Override
        public int getClientId() {
            return parent.clientId+logs;
        }
        @Override
        public void onDestroy() {
            ArrayList<ItemStack> items = new ArrayList<>();
            if(item != null)
                items.add(item.clone());
            for(int i = 0;i < logs;i++)
                items.add(new ItemStack(chunk.parent.itemRegistry.getItem(Identifier.of("bb","log")), 1));
            ItemScatterer.scatter(items, chunk.parent, new Position(x, y, z), 0.5f);
            this.isValid = false;
        }
        @Override
        public boolean isValid() {
            return isValid;
        }
        @Override
        public void onSentToPlayer(PlayerEntity player) {
            if(item != null) {
                player.send(new MessageS2C.BlockAddItem(x, y, z, 0.25f, 0.1f, 0.25f, 0, ((BlockByteItem) item.getItem()).getClientId()));
            }
        }
        private void resyncItem(){
            if(item == null)
                chunk.announceToViewersExcept(new MessageS2C.BlockRemoveItem(x, y, z, 0), null);
            else
                chunk.announceToViewersExcept(new MessageS2C.BlockAddItem(x, y, z, 0.25f, 0.26f, 0.25f, 0, ((BlockByteItem) item.getItem()).getClientId()), null);
        }
        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}
        @Override
        public void postSet(Chunk chunk, int x, int y, int z) {}
        @Override
        public void tick() {
            if(finishTime > 0){
                finishTime--;
                if(finishTime == 0){
                    logs = 0;
                    List<FirePitRecipe> recipes = chunk.parent.recipeRegistry.getRecipesForType(Identifier.of("bb", "fire_pit"));
                    for (FirePitRecipe recipe : recipes) {
                        if(recipe.input.equals(((BlockByteItem)item.getItem()).id)){
                            this.item = new ItemStack(chunk.parent.itemRegistry.getItem(recipe.output), 1);
                            resyncItem();
                            return;
                        }
                    }
                }
            }
        }
        public boolean onRightClick(PlayerEntity player){
            ItemStack hand = player.getItemInHand();
            if(finishTime == 0)
                return false;
            if(item == null){
                this.item = hand.clone(1);
                hand.removeCount(1);
                player.updateHand();
                resyncItem();
            } else if(logs < 4 && hand != null && ((BlockByteItem)hand.getItem()).id.equals(Identifier.of("bb","log"))){
                logs++;
                hand.removeCount(1);
                player.updateHand();
                updateToClients(chunk, x, y, z);
                resyncItem();
            } else if(logs == 4 && ((BlockByteItem)hand.getItem()).id.equals(Identifier.of("bb","stick")) && hand.getCount() >= 2 && finishTime == -1){
                hand.removeCount(2);
                player.updateHand();
                finishTime = 100;
            } else {
                return false;
            }
            return true;
        }
        @Override
        public float getBlockBreakingTime(ItemStack item, PlayerEntity player) {
            return 1;
        }
        @Override
        public List<ItemStack> getLoot(PlayerEntity player) {
            return null;
        }
    }
    public static class FirePitRecipe extends Recipe {
        public final Identifier input;
        public final Identifier output;
        public FirePitRecipe(Identifier id, JsonObject json) {
            super(id);
            this.input = Identifier.parse(json.get("input").getAsString());
            this.output = Identifier.parse(json.get("output").getAsString());
        }
    }
}
