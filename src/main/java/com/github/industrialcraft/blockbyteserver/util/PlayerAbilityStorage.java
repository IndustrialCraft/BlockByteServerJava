package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.world.PlayerEntity;

public class PlayerAbilityStorage {
    public final PlayerEntity player;
    private float speed;
    private EMovementType movementType;
    public PlayerAbilityStorage(PlayerEntity player) {
        this.player = player;
        this.speed = 1;
        this.movementType = EMovementType.NORMAL;
    }
    public void setSpeed(float speed){
        this.speed = speed;
        sendAbilities();
    }
    public void setMovementType(EMovementType movementType) {
        this.movementType = movementType;
        sendAbilities();
    }
    private void sendAbilities(){
        player.send(new MessageS2C.PlayerAbilities(speed, movementType));
    }

    public enum EMovementType{
        NORMAL(0),
        FLY(1),
        NO_CLIP(2);
        public final byte id;
        EMovementType(int id) {
            this.id = (byte) id;
        }
    }
}
