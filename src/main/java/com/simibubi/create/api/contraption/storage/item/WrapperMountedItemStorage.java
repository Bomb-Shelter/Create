package com.simibubi.create.api.contraption.storage.item;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

/**
 * Partial implementation of a MountedItemStorage that wraps an item handler.
 */
public abstract class WrapperMountedItemStorage<T extends SlottedStackStorage> extends MountedItemStorage {
	protected final T wrapped;

	protected WrapperMountedItemStorage(MountedItemStorageType<?> type, T wrapped) {
		super(type);
		this.wrapped = wrapped;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		this.wrapped.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlotCount() {
		return this.wrapped.getSlotCount();
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return this.wrapped.getStackInSlot(slot);
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.insertSlot(slot, resource, maxAmount, transaction);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.extractSlot(slot, resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return this.wrapped.getSlot(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.wrapped.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return this.wrapped.isItemValid(slot, resource, count);
	}

	public static ItemStackHandler copyToItemStackHandler(SlottedStorage<ItemVariant> storage) {
		ItemStack[] array = new ItemStack[storage.getSlotCount()];
		for (int i = 0; i < array.length; i++) {
			SingleSlotStorage<ItemVariant> slot = storage.getSlot(i);
			if (slot.isResourceBlank()) {
				array[i] = ItemStack.EMPTY;
			} else {
				int amount = TransferUtil.truncateLong(slot.getAmount());
				ItemStack stack = slot.getResource().toStack(amount);
				array[i] = stack;
			}
		}
		return new ItemStackHandler(array);
	}
}
