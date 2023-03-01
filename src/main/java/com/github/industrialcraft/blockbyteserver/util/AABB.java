package com.github.industrialcraft.blockbyteserver.util;

import java.util.ArrayList;
import java.util.List;

public record AABB(float x, float y, float z, float w, float h, float d) {
    public List<BlockPosition> getCollisionsOnGrid(){
        ArrayList<BlockPosition> output = new ArrayList<>();

        BlockPosition first = new Position(x, y, z).toBlockPos();
        BlockPosition second = new Position(x+w, y+h, z+d).toBlockPos();
        for(int x = first.x();x <= second.x();x++){
            for(int y = first.y();y <= second.y();y++){
                for(int z = first.z();z <= second.z();z++){
                    output.add(new BlockPosition(x, y, z));
                }
            }
        }
        return output;
    }
}
