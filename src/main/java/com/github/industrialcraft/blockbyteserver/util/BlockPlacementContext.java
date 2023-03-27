package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.blockbyteserver.world.Entity;

public class BlockPlacementContext {
    public final EHorizontalFace face;
    public final Entity placer;
    public final BlockPosition targetBlock;
    public final BlockPosition clickedBlock;
    public final EFace clickedFace;
    public BlockPlacementContext(Entity placer, BlockPosition targetBlock, BlockPosition clickedBlock, EFace clickedFace) {
        this.placer = placer;
        this.targetBlock = targetBlock;
        this.clickedBlock = clickedBlock;
        this.clickedFace = clickedFace;
        var playerPos = placer.getPosition().toBlockPos();
        boolean frontBack = Math.abs(playerPos.z() - targetBlock.x()) > Math.abs(playerPos.x() - targetBlock.z());
        if (frontBack) {
            if (playerPos.z() - targetBlock.z() < 0) {
                face = EHorizontalFace.FRONT;
            } else {
                face = EHorizontalFace.BACK;
            }
        } else {
            if (playerPos.x() - targetBlock.x() < 0) {
                face = EHorizontalFace.LEFT;
            } else {
                face = EHorizontalFace.RIGHT;
            }
        }
    }
}
