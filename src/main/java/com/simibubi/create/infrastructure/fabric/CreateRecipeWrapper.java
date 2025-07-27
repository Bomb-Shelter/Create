package com.simibubi.create.infrastructure.fabric;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class CreateRecipeWrapper implements RecipeInput {
	protected final SlottedStackStorage inv;

	public CreateRecipeWrapper(SlottedStackStorage inv) {
		this.inv = inv;
	}

	/**
	 * Returns the size of this inventory.
	 */
	@Override
	public int size() {
		return inv.getSlotCount();
	}

	/**
	 * Returns the stack in this slot. This stack should be a modifiable reference, not a copy of a stack in your inventory.
	 */
	@Override
	public ItemStack getItem(int slot) {
		return inv.getStackInSlot(slot);
	}
}
