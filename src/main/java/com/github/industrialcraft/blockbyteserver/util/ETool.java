package com.github.industrialcraft.blockbyteserver.util;

public enum ETool {
    AXE,
    KNIFE,
    PICKAXE,
    HAMMER,
    WRENCH,
    SHOVEL,
    HOE;
    public static ETool fromString(String name){
        for (ETool tool : values()) {
            if(tool.name().equalsIgnoreCase(name))
                return tool;
        }
        return null;
    }
}
