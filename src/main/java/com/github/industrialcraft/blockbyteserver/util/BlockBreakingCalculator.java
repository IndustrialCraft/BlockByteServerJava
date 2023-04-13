package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.inventorysystem.ItemStack;

public class BlockBreakingCalculator {
    public static float calculateBlockBreakingTime(ItemStack item, ETool tool, int minToolLevel, float blockHardness){//todo:beautify
        if(tool == null)
            return blockHardness;
        if(item == null) {
            if(minToolLevel == 0){
                return blockHardness;
            } else {
                return -1;
            }
        }
        if(item.getItem() instanceof ITool toolItem){
            if(tool != toolItem.getToolType()) {
                if(minToolLevel == 0){
                    return blockHardness;
                } else {
                    return -1;
                }
            }
            if(minToolLevel > toolItem.getToolLevel())
                return -1;
            return ((float)blockHardness)/toolItem.getToolSpeed();
        } else {
            return -1;
        }
    }
    public static boolean calculateWhetherBlockDrops(ItemStack item, ETool tool, int minToolLevel, float blockHardness){
        if(tool == null)
            return true;
        if(item == null)
            return false;
        if(item.getItem() instanceof ITool toolItem){
            if(tool != toolItem.getToolType())
                return false;
            if(minToolLevel > toolItem.getToolLevel())
                return false;
            return true;
        } else {
            return false;
        }
    }
}
