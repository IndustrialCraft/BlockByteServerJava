package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.world.Chunk;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;

public class SimpleBlockInstance<T extends SimpleBlock> extends AbstractBlockInstance<T>{
    public SimpleBlockInstance(T parent) {
        super(parent);
    }
    @Override
    public int getClientId() {
        return parent.getDefaultClientId();
    }
    @Override
    public void onDestroy(){

    }
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void onSentToPlayer(PlayerEntity player) {}
    @Override
    public void onNeighborUpdate(BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance, EFace face) {}

    @Override
    public void postSet(Chunk chunk, int x, int y, int z) {

    }
}
