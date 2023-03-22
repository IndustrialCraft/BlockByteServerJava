package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.identifier.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EntityRegistry {
    private HashMap<Identifier,EntityRenderData> entities;
    private int entityIdGenerator;
    public EntityRegistry() {
        this.entities = new HashMap<>();
        this.entityIdGenerator = 0;
    }
    public void register(Identifier identifier, String model, String texture, float hitboxW, float hitboxH, float hitboxD){
        if(entities.containsKey(identifier))
            throw new IllegalStateException("entity " + identifier + " already registered");
        entities.put(identifier, new EntityRenderData(this.entityIdGenerator++, model, texture, hitboxW, hitboxH, hitboxD));
    }
    public List<EntityRenderData> getEntities(){
        return entities.values().stream().toList();
    }
    public EntityRenderData getByIdentifier(Identifier identifier){
        return entities.get(identifier);
    }
    public record EntityRenderData(int clientId, String model, String texture, float hitboxW, float hitboxH, float hitboxD){

    }
}
