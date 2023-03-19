package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonObject;

public abstract class Recipe {
    public final Identifier id;
    protected Recipe(Identifier id) {
        this.id = id;
    }
}
