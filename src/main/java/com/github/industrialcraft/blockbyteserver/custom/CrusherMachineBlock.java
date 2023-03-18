package com.github.industrialcraft.blockbyteserver.custom;

import com.github.industrialcraft.blockbyteserver.content.Block;
import com.github.industrialcraft.blockbyteserver.content.BlockInstance;
import com.github.industrialcraft.blockbyteserver.loot.LootTable;
import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.ITicking;
import com.github.industrialcraft.blockbyteserver.world.*;
import com.google.gson.JsonObject;

public class CrusherMachineBlock extends Block {
    public static final int MAX_PROGRESS = 100;
    public CrusherMachineBlock(MessageS2C.InitializeContent.BlockRenderData renderData, int clientId, LootTable lootTable) {
        super(renderData, clientId, lootTable);
    }
    @Override
    public BlockInstance createBlockInstance(Chunk chunk, int x, int y, int z) {
        return new CrusherMachineBlockInstance(this, x + (chunk.position.x()*16), y + (chunk.position.y()*16), z + (chunk.position.z()*16));
    }
    @Override
    public boolean onRightClick(World world, BlockPosition blockPosition, BlockInstance instance, PlayerEntity player) {
        player.setGui(new CrusherMachineGUI(player, instance));
        return true;
    }
    public static class CrusherMachineBlockInstance extends BlockInstance<CrusherMachineBlock> implements ITicking {
        public final int x;
        public final int y;
        public final int z;
        public final BasicVersionedInventory inventory;
        public int progress;
        public CrusherMachineBlockInstance(CrusherMachineBlock parent, int x, int y, int z) {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.inventory = new BasicVersionedInventory(2, (inventory1, is) -> {}, this);
        }

        @Override
        public boolean isUnique() {
            return true;
        }

        @Override
        public void tick() {
            progress++;
            if(progress > MAX_PROGRESS)
                progress = 0;
        }
    }
    public static class CrusherMachineGUI extends InventoryGUI {
        public final BlockInstance<CrusherMachineBlock> block;
        private int lastSyncedProgress;
        public CrusherMachineGUI(PlayerEntity player, BlockInstance<CrusherMachineBlock> block) {
            super(player);
            this.block = block;
            this.slots.put("gui_input", new Slot(((CrusherMachineBlockInstance)block).inventory, 0, -0.2f, 0));
            this.slots.put("gui_output", new Slot(((CrusherMachineBlockInstance)block).inventory, 1, 0.2f, 0));
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
}
