package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.BlockPosition;
import com.github.industrialcraft.blockbyteserver.util.EFace;
import com.github.industrialcraft.blockbyteserver.world.World;

public class BlockHelper {
    public static boolean needSupportCanPlace(World world, int x, int y, int z){
        return !world.getBlock(new BlockPosition(x, y-1, z)).parent.isNoCollide();
    }
    public static void needsSupportNeighborUpdate(World world, BlockPosition position, AbstractBlockInstance newInstance, EFace face) {
        if(face == EFace.Down && newInstance.parent.isNoCollide()){
            world.setBlock(position, SimpleBlock.AIR, null);
        }
    }
}
