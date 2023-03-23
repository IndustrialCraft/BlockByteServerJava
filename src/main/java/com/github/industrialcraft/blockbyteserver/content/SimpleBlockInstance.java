package com.github.industrialcraft.blockbyteserver.content;

public class SimpleBlockInstance<T extends SimpleBlock> extends AbstractBlockInstance<T>{
    private boolean valid;
    public SimpleBlockInstance(T parent) {
        super(parent);
        this.valid = true;
    }
    @Override
    public int getClientId() {
        return parent.getDefaultClientId();
    }
    @Override
    public void invalidate(){
        this.valid = false;
    }
    @Override
    public boolean isValid() {
        return valid;
    }
    @Override
    public boolean isUnique(){
        return false;
    }
}
