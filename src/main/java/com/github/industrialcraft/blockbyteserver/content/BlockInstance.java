package com.github.industrialcraft.blockbyteserver.content;

public class BlockInstance<T extends Block> {
    public final T parent;
    private boolean valid;
    public BlockInstance(T parent) {
        this.parent = parent;
        this.valid = true;
    }
    public void invalidate(){
        this.valid = false;
    }
    public boolean isValid() {
        return valid;
    }
    public boolean isUnique(){
        return false;
    }
}
