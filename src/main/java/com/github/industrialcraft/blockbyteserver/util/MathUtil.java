package com.github.industrialcraft.blockbyteserver.util;

public class MathUtil {
    public static float lerp(float a, float b, float v){
        return b*v + (a*(1-v));
    }
}
