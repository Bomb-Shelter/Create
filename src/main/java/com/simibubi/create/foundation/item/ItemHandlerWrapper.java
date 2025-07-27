package com.simibubi.create.foundation.item;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class ItemHandlerWrapper implements SlottedStackStorage {

	private SlottedStackStorage wrapped;

	public ItemHandlerWrapper(SlottedStackStorage wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public int getSlotCount() {
		return wrapped.getSlotCount();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return wrapped.getStackInSlot(slot);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return wrapped.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public int getSlotLimit(int slot) {
		return wrapped.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return wrapped.isItemValid(slot, resource, count);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return wrapped.getSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		wrapped.setStackInSlot(slot, stack);
	}

}
