package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.identifier.Identifier;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RecipeRegistry {
    private final HashMap<Identifier, IRecipeCreator> recipeCreators;
    private final HashMap<Identifier, Recipe> recipes;
    private final HashMap<Identifier, ArrayList<Recipe>> recipesByType;
    public RecipeRegistry() {
        this.recipeCreators = new HashMap<>();
        this.recipes = new HashMap<>();
        this.recipesByType = new HashMap<>();
    }
    public void registerCreator(Identifier type, IRecipeCreator recipeCreator){
        if(recipeCreators.containsKey(type))
            throw new IllegalStateException("recipe registry already contains type " + type);
        recipeCreators.put(type, recipeCreator);
        recipesByType.put(type, new ArrayList<>());
    }
    public void loadDirectory(File dir){
        for (File file : dir.listFiles()) {
            if(file.isFile() && file.getName().endsWith(".json")){
                String name = file.getName().replace(".json", "");
                Identifier id = Identifier.parse(name);
                try {
                    FileReader reader = new FileReader(file);
                    loadRecipe(id, JsonParser.parseReader(reader).getAsJsonObject());
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public <T extends Recipe> List<T> getRecipesForType(Identifier type){
        return (List<T>) Collections.unmodifiableList(recipesByType.get(type));
    }
    public void loadRecipe(Identifier id, JsonObject json){
        if(recipes.containsKey(id))
            throw new IllegalStateException("recipe " + id + " already registered");
        Identifier type = Identifier.parse(json.get("type").getAsString());
        IRecipeCreator recipeCreator = recipeCreators.get(type);
        if(recipeCreator == null)
            throw new IllegalStateException("recipe creator for type " + type + " not found");
        Recipe recipe = recipeCreator.create(id, json);
        this.recipes.put(id, recipe);
        this.recipesByType.get(type).add(recipe);
    }
    @FunctionalInterface
    public interface IRecipeCreator {
        Recipe create(Identifier id, JsonObject json);
    }
}
