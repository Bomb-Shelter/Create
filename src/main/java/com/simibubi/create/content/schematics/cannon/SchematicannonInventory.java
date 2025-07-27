package com.simibubi.create.content.schematics.cannon;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SchematicannonInventory extends ItemStackHandler {
	private final SchematicannonBlockEntity blockEntity;

	public SchematicannonInventory(SchematicannonBlockEntity blockEntity) {
		super(5);
		this.blockEntity = blockEntity;
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		blockEntity.setChanged();
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		ItemStack stack = resource.toStack(count);
		switch (slot) {
		case 0: // Blueprint Slot
			return AllItems.SCHEMATIC.isIn(stack);
		case 1: // Blueprint output
			return false;
		case 2: // Book input
			return AllBlocks.CLIPBOARD.isIn(stack) || stack.is(Items.BOOK)
				|| stack.is(Items.WRITTEN_BOOK);
		case 3: // Material List output
			return false;
		case 4: // Gunpowder
			return stack.is(Items.GUNPOWDER);
		default:
			return super.isItemValid(slot, resource, count);
		}
	}
}
