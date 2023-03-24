package com.github.industrialcraft.blockbyteserver.content;

public abstract class AbstractBlockInstance<T extends AbstractBlock> {
    public final T parent;
    public AbstractBlockInstance(T parent) {
        this.parent = parent;
    }
    public abstract int getClientId();
    public abstract void onDestroy();
    public abstract boolean isValid();
}
