package com.simibubi.create.content.processing.recipe;

import java.util.function.Consumer;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ProcessingInventory extends ItemStackHandler {
	public float remainingTime;
	public float recipeDuration;
	public boolean appliedRecipe;
	public Consumer<ItemStack> callback;
	private boolean limit;

	public ProcessingInventory(Consumer<ItemStack> callback) {
		super(32);
		this.callback = callback;
	}

	public ProcessingInventory withSlotLimit(boolean limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public int getSlotLimit(int slot) {
		return !limit ? super.getSlotLimit(slot) : 1;
	}

	public void clear() {
		for (int i = 0; i < getSlotCount(); i++)
			setStackInSlot(i, ItemStack.EMPTY);
		remainingTime = 0;
		recipeDuration = 0;
		appliedRecipe = false;
	}

	public boolean isEmpty() {
		for (int i = 0; i < getSlotCount(); i++)
			if (!getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		boolean isFirstSlot = this.getSlot(0).getAmount() < this.getSlot(0).getCapacity() && this.getSlot(0).getResource().equals(resource);
		long lastAmount = this.getSlot(0).getAmount();
		long inserted = super.insert(resource, maxAmount, transaction);
		ItemVariant newResource = this.getSlot(0).getResource();

		if (isFirstSlot && !(inserted == maxAmount && newResource.equals(resource)))
			callback.accept(newResource.toStack((int) (lastAmount - inserted)));

		return inserted;
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack insertItem = getStackInSlot(slot);
		long inserted = super.insertSlot(slot, resource, maxAmount, transaction);
		if (slot == 0 && !(insertItem.getCount() == maxAmount && ItemStack.isSameItem(insertItem, CreateTransferUtil.getLimitedStack(resource, maxAmount))))
			callback.accept(getStackInSlot(slot));
		return inserted;
	}

	@Override
	public @NotNull CompoundTag serializeNBT(@NotNull HolderLookup.Provider registries) {
		CompoundTag nbt = super.serializeNBT(registries);
		nbt.putFloat("ProcessingTime", remainingTime);
		nbt.putFloat("RecipeTime", recipeDuration);
		nbt.putBoolean("AppliedRecipe", appliedRecipe);
		return nbt;
	}

	@Override
	public void deserializeNBT(@NotNull HolderLookup.Provider registries, CompoundTag nbt) {
		remainingTime = nbt.getFloat("ProcessingTime");
		recipeDuration = nbt.getFloat("RecipeTime");
		appliedRecipe = nbt.getBoolean("AppliedRecipe");
		super.deserializeNBT(registries, nbt);
		if (isEmpty())
			appliedRecipe = false;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return slot == 0 && isEmpty();
	}

}
