package com.simibubi.create.foundation.item;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.blockEntity.LegacyRecipeWrapper;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.core.util.INBTSerializable;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class SmartInventory extends LegacyRecipeWrapper
	implements SlottedStackStorage, INBTSerializable<CompoundTag> {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected SyncedStackHandler wrapped;
	protected int stackSize;

	public SmartInventory(int slots, SyncedBlockEntity be) {
		this(slots, be, 64, false);
	}

	public SmartInventory(int slots, SyncedBlockEntity be, int stackSize, boolean stackNonStackables) {
		super(new SyncedStackHandler(slots, be, stackNonStackables, stackSize));
		this.stackNonStackables = stackNonStackables;
		insertionAllowed = true;
		extractionAllowed = true;
		this.stackSize = stackSize;
		wrapped = (SyncedStackHandler) inv;
	}

	public SmartInventory withMaxStackSize(int maxStackSize) {
		stackSize = maxStackSize;
		wrapped.stackSize = maxStackSize;
		return this;
	}

	public SmartInventory whenContentsChanged(Consumer<Integer> updateCallback) {
		((SyncedStackHandler) inv).whenContentsChange(updateCallback);
		return this;
	}

	public SmartInventory allowInsertion() {
		insertionAllowed = true;
		return this;
	}

	public SmartInventory allowExtraction() {
		extractionAllowed = true;
		return this;
	}

	public SmartInventory forbidInsertion() {
		insertionAllowed = false;
		return this;
	}

	public SmartInventory forbidExtraction() {
		extractionAllowed = false;
		return this;
	}

	@Override
	public int getSlotCount() {
		return inv.getSlotCount();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return new SmartInventorySingleSlot(inv.getSlot(slot));
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return inv.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!extractionAllowed)
			return 0;
		if (stackNonStackables) {
			ItemStack extractItem = CreateTransferUtil.getLimitedStack(resource, maxAmount);
			long extracted = CreateTransferUtil.simulateExtract(inv, resource, maxAmount);
			if (extracted > 0 && extractItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) < extractItem.getCount())
				maxAmount = extractItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
		}
		return inv.extract(resource, maxAmount, transaction);
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(inv.getSlotLimit(slot), stackSize);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return inv.isItemValid(slot, resource, count);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		((SyncedStackHandler) inv).setStackInSlot(slot, stack);
	}

	public int getStackLimit(int slot, @Nonnull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		return getInv().serializeNBT(registries);
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
		getInv().deserializeNBT(registries, nbt);
	}

	private SyncedStackHandler getInv() {
		return (SyncedStackHandler) inv;
	}

	private class SmartInventorySingleSlot implements SingleSlotStorage<ItemVariant> {
		private final SingleSlotStorage<ItemVariant> wrapped;

		public SmartInventorySingleSlot(SingleSlotStorage<ItemVariant> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return wrapped.insert(resource, maxAmount, transaction);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!extractionAllowed)
				return 0;
			if (stackNonStackables) {
				ItemStack extractItem = CreateTransferUtil.getLimitedStack(resource, maxAmount);
				long extracted = CreateTransferUtil.simulateExtract(wrapped, resource, maxAmount);
				if (extracted > 0 && extractItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) < extractItem.getCount())
					maxAmount = extractItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
			}
			return wrapped.extract(resource, maxAmount, transaction);
		}

		@Override
		public boolean isResourceBlank() {
			return wrapped.isResourceBlank();
		}

		@Override
		public ItemVariant getResource() {
			return wrapped.getResource();
		}

		@Override
		public long getAmount() {
			return wrapped.getAmount();
		}

		@Override
		public long getCapacity() {
			return wrapped.getCapacity();
		}
	}

	private static class SyncedStackHandler extends ItemStackHandler {

		private SyncedBlockEntity blockEntity;
		private boolean stackNonStackables;
		private int stackSize;
		private Consumer<Integer> updateCallback;

		public SyncedStackHandler(int slots, SyncedBlockEntity be, boolean stackNonStackables, int stackSize) {
			super(slots);
			this.blockEntity = be;
			this.stackNonStackables = stackNonStackables;
			this.stackSize = stackSize;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			if (updateCallback != null)
				updateCallback.accept(slot);
			blockEntity.notifyUpdate();
		}

		@Override
		public int getSlotLimit(int slot) {
			return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
		}

		public void whenContentsChange(Consumer<Integer> updateCallback) {
			this.updateCallback = updateCallback;
		}

	}

}
