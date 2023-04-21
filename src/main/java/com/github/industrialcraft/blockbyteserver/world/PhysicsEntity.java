package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.content.AbstractBlockInstance;
import com.github.industrialcraft.blockbyteserver.content.SimpleBlock;
import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.Position;

public abstract class PhysicsEntity extends Entity{
    private float velocityX;
    private float velocityY;
    private float velocityZ;
    public PhysicsEntity(Position position, World world) {
        super(position, world);
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
    }
    public void addVelocity(float x, float y, float z){
        this.velocityX += x;
        this.velocityY += y;
        this.velocityZ += z;
    }
    @Override
    public void tick() {
        Position newPosition = getPosition();
        if(collidesAt(newPosition.x() + velocityX, newPosition.y(), newPosition.z())){
            velocityX = 0;
        }
        if(collidesAt(newPosition.x() + velocityX, newPosition.y() + velocityY, newPosition.z())){
            velocityY = 0;
        }
        if(collidesAt(newPosition.x() + velocityX, newPosition.y() + velocityY, newPosition.z() + velocityZ)){
            velocityZ = 0;
        }
        teleport(new Position(newPosition.x() + velocityX, newPosition.y() + velocityY, newPosition.z() + velocityZ));
        this.velocityX *= 0.90;
        this.velocityY *= 0.90;
        this.velocityZ *= 0.90;

        this.velocityY -= 1/20f;
    }
    private boolean collidesAt(float x, float y, float z){
        var bb = getBoundingBox().getCollisionsOnGrid(x, y, z);
        for (BlockPosition blockPosition : bb) {
            AbstractBlockInstance blockInstance = chunk.parent.getBlock(blockPosition);
            if(blockInstance == null || (!blockInstance.parent.isNoCollide())){
                return true;
            }
        }
        return false;
    }
}
