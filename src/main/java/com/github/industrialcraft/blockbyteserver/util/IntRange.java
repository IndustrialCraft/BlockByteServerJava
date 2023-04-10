package com.github.industrialcraft.blockbyteserver.util;

import java.util.Random;

public record IntRange(int from, int to) {
    public int generate(Random random){
        return random.nextInt(to-from)+from;
    }
    public boolean contains(int value){
        return value >= from && value < to;
    }
}
