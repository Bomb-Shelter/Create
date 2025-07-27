package com.simibubi.create.foundation.recipe;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class DummyCraftingContainer extends TransientCraftingContainer {

	private final NonNullList<ItemStack> inv;

	public DummyCraftingContainer(Storage<ItemVariant> itemHandler, long[] extractedItemsFromSlot) {
		super(null, 0, 0);

		this.inv = createInventory(itemHandler, extractedItemsFromSlot);
	}

	@Override
	public int getContainerSize() {
		return this.inv.size();
	}

	@Override
	public boolean isEmpty() {
		for (int slot = 0; slot < this.getContainerSize(); slot++) {
			if (!this.getItem(slot).isEmpty())
				return false;
		}

		return true;
	}

	@Override
	public @NotNull ItemStack getItem(int slot) {
		return slot >= this.getContainerSize() ? ItemStack.EMPTY : this.inv.get(slot);
	}

	@Override
	public @NotNull ItemStack removeItemNoUpdate(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack removeItem(int slot, int count) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack stack) {}

	@Override
	public void clearContent() {}

	@Override
	public void fillStackedContents(@NotNull StackedContents helper) {}

	private static NonNullList<ItemStack> createInventory(Storage<ItemVariant> itemHandler, long[] extractedItemsFromSlot) {
		NonNullList<ItemStack> inv = NonNullList.create();

		int slot = 0;
		for (StorageView<ItemVariant> view : itemHandler) {
			ItemStack stack = CreateTransferUtil.getLimitedStack(view.getResource(), view.getAmount());

			if (stack.isEmpty())
				continue;

			for (long i = 0; i < extractedItemsFromSlot[slot]; i++) {
				ItemStack stackCopy = stack.copy();
				stackCopy.setCount(1);

				inv.add(stackCopy);
			}

			slot++;
		}

		return inv;
	}

}
