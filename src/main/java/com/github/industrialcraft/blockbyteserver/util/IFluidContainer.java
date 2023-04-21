package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.blockbyteserver.content.Fluid;

public interface IFluidContainer {
    Fluid getFluid();
    void setFluid(Fluid fluid, int amount);
    int getFluidAmount();
    void setFluidAmount(int amount);
    int getFluidCapacity();
}
