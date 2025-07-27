package com.simibubi.create.content.decoration;

import net.fabricmc.fabric.api.registry.FuelRegistry;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

public class CardboardBlockItem extends BlockItem {

	public CardboardBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
		FuelRegistry.INSTANCE.add(this, 4000);
	}

	/*@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return 4000;
	}*/

}
