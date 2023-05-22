package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.world.Entity;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;

public class AnimationController {
    private final Entity parent;
    private String currentAnimation;
    private float animationStartTime;
    private String defaultAnimation;
    public AnimationController(Entity parent, String defaultAnimation) {
        this.parent = parent;
        this.defaultAnimation = defaultAnimation;
        setAnimation(defaultAnimation);
    }
    public void setAnimation(String animation){
        if(animation == null)
            animation = defaultAnimation;
        this.currentAnimation = animation;
        this.animationStartTime = System.currentTimeMillis()/1000f;
        parent.getChunk().announceToViewersExcept(new MessageS2C.EntityAnimation(parent.clientId, animation), (parent instanceof PlayerEntity player)?player:null);
    }
    public String getCurrentAnimation() {
        return currentAnimation;
    }
    public float getCurrentAnimationTime() {
        return (System.currentTimeMillis()/1000f)-animationStartTime;
    }
}
