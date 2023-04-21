package com.github.industrialcraft.blockbyteserver.content;

import com.github.industrialcraft.blockbyteserver.util.IFluidContainer;
import com.github.industrialcraft.blockbyteserver.world.World;
import com.github.industrialcraft.identifier.Identifier;
import com.github.industrialcraft.inventorysystem.ItemStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.regex.Pattern;

public abstract class Recipe {
    public final Identifier id;
    protected Recipe(Identifier id) {
        this.id = id;
    }
    public static IRecipePart fromJson(JsonElement json, ItemRegistry itemRegistry, FluidRegistry fluidRegistry){
        String raw = json.getAsString();
        boolean fluid = false;
        if(raw.startsWith("|")){
            raw = raw.replaceFirst(Pattern.quote("|"),"");
            fluid = true;
        }
        String[] parts = raw.split(Pattern.quote(";"));
        if(parts.length < 1 || parts.length > 2)
            throw new IllegalArgumentException("recipe part must be split on ; to 1 or 2 parts");
        int count = 1;
        if(parts.length == 2){
            count = Integer.parseInt(parts[1]);
        }
        Identifier identifier = Identifier.parse(parts[0]);
        if(fluid){
            return new FluidRecipePart(fluidRegistry.getFluid(identifier), count);
        } else {
            return new ItemStackRecipePart(itemRegistry.getItem(identifier), count);
        }
    }
    public interface IRecipePart{
        boolean matches(ItemStack stack);
        void consume(ItemStack stack);
    }
    public record ItemStackRecipePart(BlockByteItem item, int count) implements IRecipePart{
        public ItemStack create(){
            return new ItemStack(item, count);
        }
        @Override
        public boolean matches(ItemStack stack){
            if(stack == null)
                return false;
            return stack.getItem() == item && stack.getCount() >= count;
        }
        @Override
        public void consume(ItemStack stack) {
            if(!matches(stack))
                throw new IllegalStateException();
            stack.removeCount(count);
        }
    }
    public record FluidRecipePart(Fluid fluid, int amount) implements IRecipePart{
        @Override
        public boolean matches(ItemStack stack) {
            if(stack == null)
                return false;
            if(stack.getData() instanceof IFluidContainer fluidContainer){
                if(fluidContainer.getFluid() != fluid)
                    return false;
                return fluidContainer.getFluidAmount() >= amount;
            }
            return false;
        }
        @Override
        public void consume(ItemStack stack) {
            if(!matches(stack))
                throw new IllegalStateException();
            IFluidContainer fluidContainer = (IFluidContainer)stack.getData();
            fluidContainer.setFluidAmount(fluidContainer.getFluidAmount()-amount);
        }
    }
}
