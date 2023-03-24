package com.github.industrialcraft.blockbyteserver.util;

import com.github.industrialcraft.inventorysystem.Inventory;

public interface IInventoryBlock {
    Inventory getInput(EFace face);
    Inventory getOutput(EFace face);
}
