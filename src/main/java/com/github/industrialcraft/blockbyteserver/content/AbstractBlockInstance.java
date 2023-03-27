package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;

public abstract class AbstractBlockInstance<T extends AbstractBlock> {
    public final T parent;
    public AbstractBlockInstance(T parent) {
        this.parent = parent;
    }
    public abstract int getClientId();
    public abstract void onDestroy();
    public abstract boolean isValid();
    public abstract void onSentToPlayer(PlayerEntity player);
    public abstract void onNeighborUpdate(BlockPosition position, AbstractBlockInstance previousInstance, AbstractBlockInstance newInstance);
}
