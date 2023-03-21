package com.github.industrialcraft.blockbyteserver.util;

import java.util.ArrayList;
import java.util.List;

public record AABB(float w, float h, float d) {
    public List<BlockPosition> getCollisionsOnGrid(float x, float y, float z){
        ArrayList<BlockPosition> output = new ArrayList<>();

        BlockPosition first = new Position(x, y, z).toBlockPos();
        BlockPosition second = new Position(x+w, y+h, z+d).toBlockPos();
        for(int bx = first.x();bx <= second.x();bx++){
            for(int by = first.y();by <= second.y();by++){
                for(int bz = first.z();bz <= second.z();bz++){
                    output.add(new BlockPosition(bx, by, bz));
                }
            }
        }
        return output;
    }
}
