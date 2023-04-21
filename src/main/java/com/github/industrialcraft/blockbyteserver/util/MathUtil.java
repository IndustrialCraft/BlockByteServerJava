package com.github.industrialcraft.blockbyteserver.util;

public class MathUtil {
    public static float lerp(float a, float b, float v){
        return b*v + (a*(1-v));
    }
    public static int clamp(int value, int min, int max){return Math.min(Math.max(value, min), max);}
}
