package com.hbm.recipes;

import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
//the old recipe handler is straight up just hell, FUCK ITS HORRIBLE so im just starting from zero (i WILL move it all over someday lmao)

// never fucking mind this mod is confusing as fuck im not even gonna try to figure out what the main class is lul
public class SmeltingRecipes {
    public static void init()
    {
        GameRegistry.addSmelting(new ItemStack(ModItems.scrap_plastic), new ItemStack(ModItems.ingot_polymer), 0.5F);
    }
}
