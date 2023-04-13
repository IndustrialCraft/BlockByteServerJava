package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.*;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.util.IInventoryBlock;
import com.github.industrialcraft.blockbyteserver.util.ITicking;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.Inventory;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CrusherMachineBlock extends SimpleBlock {
    public static final int MAX_PROGRESS = 100;
    public CrusherMachineBlock(BlockRegistry.BlockRenderData renderData, AtomicInteger clientId, LootTable lootTable) {
        super(renderData, clientId, lootTable, Identifier.of("bb","crusher"), null, 0, 1, false);
    }
    @Override
    public SimpleBlockInstance createBlockInstance(Chunk chunk, int x, int y, int z, Object data) {
        return new CrusherMachineBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16), chunk.parent);
    }
    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, AbstractBlockInstance instance, PlayerEntity player) {
        player.setGui(new CrusherMachineGUI(player, instance));
        return true;
    }
    public static class CrusherMachineBlockInstance extends SimpleBlockInstance<CrusherMachineBlock> implements ITicking, IInventoryBlock {
        public final int x;
        public final int y;
        public final int z;
        public final World world;
        public final BasicVersionedInventory inputInventory;
        public final BasicVersionedInventory outputInventory;
        public int progress;
        private CrusherRecipe currentRecipe;
        private boolean isValid;
        public CrusherMachineBlockInstance(CrusherMachineBlock parent, int x, int y, int z, World world) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.inputInventory = new BasicVersionedInventory(1, (inventory1, is) -> {}, this){
                @Override
                public ItemStack setAt(int index, ItemStack itemStack) {
                    itemStack = super.setAt(index, itemStack);
                    tryStartRecipe();
                    return itemStack;
                }
            };
            this.outputInventory = new BasicVersionedInventory(1, (inventory1, is) -> {}, this);
            this.isValid = true;
        }
        private void tryStartRecipe(){
            if(inputInventory == null)
                return;
            ItemStack inputSlot = inputInventory.getAt(0);
            if(currentRecipe == null && inputSlot != null){
                List<CrusherRecipe> recipes = world.recipeRegistry.getRecipesForType(Identifier.of("bb", "crushing"));
                for(CrusherRecipe recipe : recipes){
                    if(world.itemRegistry.getItem(recipe.input) == inputSlot.getItem()){
                        inputSlot.removeCount(1);
                        currentRecipe = recipe;
                        return;
                    }
                }
            }
        }
        @Override
        public void onDestroy() {
            this.isValid = false;
        }
        @Override
        public boolean isValid() {
            return isValid;
        }

        @Override
        public void onSentToPlayer(PlayerEntity player) {}

        @Override
        public void onNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}

        @Override
        public void tick() {
            if(currentRecipe != null) {
                progress++;
                if (progress > MAX_PROGRESS){
                    ItemStack is = new ItemStack(world.itemRegistry.getItem(currentRecipe.output), 1);
                    ItemStack inventoryStack = outputInventory.getAt(0);
                    if(inventoryStack == null){
                        outputInventory.setAt(0, is);
                    } else {
                        if(inventoryStack.stacks(is)){
                            inventoryStack.addCount(is.getCount());
                            outputInventory.setAt(0, inventoryStack);
                        }
                    }
                    progress = 0;
                    currentRecipe = null;
                    tryStartRecipe();
                }
            }
        }

        @Override
        public Inventory getInput(EFace face) {
            return inputInventory;
        }
        @Override
        public Inventory getOutput(EFace face) {
            return outputInventory;
        }
    }
    public static class CrusherMachineGUI extends InventoryGUI {
        public final AbstractBlockInstance<CrusherMachineBlock> block;
        private int lastSyncedProgress;
        public CrusherMachineGUI(PlayerEntity player, AbstractBlockInstance<CrusherMachineBlock> block) {
            super(player, ((CrusherMachineBlockInstance)block).inputInventory);
            this.block = block;
            this.slots.put("gui_input", new Slot(((CrusherMachineBlockInstance)block).inputInventory, 0, -0.2f, 0, player.inventory, false));
            this.slots.put("gui_output", new Slot(((CrusherMachineBlockInstance)block).outputInventory, 0, 0.2f, 0, player.inventory, true));
            this.lastSyncedProgress = -1;
        }
        @Override
        public void onOpen() {
            super.onOpen();
            {
                JsonObject json = new JsonObject();
                json.addProperty("id", "gui_progress_background");
                json.addProperty("type", "setElement");
                json.addProperty("element_type", "image");
                json.addProperty("texture", "arrow");
                json.addProperty("y", -0.01f);
                json.addProperty("x", -0.09f);
                json.addProperty("z", -1);
                json.addProperty("w", 0.22f);
                json.addProperty("h", 0.12f);
                json.add("color", MessageS2C.GUIData.createFloatArray(1f, 0f, 0f, 1f));
                player.send(new MessageS2C.GUIData(json));
            }
            {
                JsonObject json = new JsonObject();
                json.addProperty("id", "gui_progress");
                json.addProperty("type", "setElement");
                json.addProperty("element_type", "image");
                json.addProperty("texture", "arrow");
                json.addProperty("y", 0f);
                json.addProperty("x", -0.08f);
                json.addProperty("w", 0.2f);
                json.addProperty("h", 0.1f);
                player.send(new MessageS2C.GUIData(json));
            }
        }
        @Override
        public boolean onTick() {
            if(!block.isValid())
                return false;
            CrusherMachineBlockInstance instance = (CrusherMachineBlockInstance) block;
            if(lastSyncedProgress != instance.progress){
                lastSyncedProgress = instance.progress;
                float progressbar = ((float)lastSyncedProgress)/MAX_PROGRESS;
                {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", "gui_progress");
                    json.addProperty("type", "editElement");
                    json.addProperty("data_type", "slice");
                    json.add("slice", MessageS2C.GUIData.createFloatArray(0, 0, progressbar, 1));
                    player.send(new MessageS2C.GUIData(json));
                }
                {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", "gui_progress");
                    json.addProperty("type", "editElement");
                    json.addProperty("data_type", "dimension");
                    json.add("dimension", MessageS2C.GUIData.createFloatArray(0.2f*progressbar, 0.1f));
                    player.send(new MessageS2C.GUIData(json));
                }
            }
            var blockPos = player.getPosition().toBlockPos();
            int xDiff = instance.x - blockPos.x();
            int yDiff = instance.y - blockPos.y();
            int zDiff = instance.z - blockPos.z();
            return (xDiff*xDiff)+(yDiff*yDiff)+(zDiff*zDiff) < 25;
        }
    }
    public static class CrusherRecipe extends Recipe{
        public final Identifier input;
        public final Identifier output;
        public CrusherRecipe(Identifier id, JsonObject json) {
            super(id);
            this.input = Identifier.parse(json.get("input").getAsString());
            this.output = Identifier.parse(json.get("output").getAsString());
        }
    }
}
