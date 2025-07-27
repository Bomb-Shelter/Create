package com.simibubi.create.content.contraptions.mounted;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.SingleSlotStorageImpl;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemHandlerModifiableFromIInventory /*implements SlottedStackStorage*/ {
	// Fabric: don't need to spend time on this tbh
	/*private final Container inventory;

	public ItemHandlerModifiableFromIInventory(Container inventory) {
		this.inventory = inventory;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		inventory.setItem(slot, stack);
	}

	@Override
	public int getSlotCount() {
		return inventory.getContainerSize();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		var stack = inventory.getItem(slot);
		return new SingleSlotStorageImpl<>(ItemVariant.of(stack), stack.getCount(), inventory.getMaxStackSize(stack));
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.getItem(slot);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (maxAmount == 0)
			return 0;
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		if (!isItemValid(slot, stack))
			return stack;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		int limit = getStackLimit(slot, stack);

		if (!existing.isEmpty())
		{
			if (!ItemStack.isSameItemSameComponents(stack, existing))
				return stack;

			limit -= existing.getCount();
		}

		if (limit <= 0)
			return stack;

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate)
		{
			if (existing.isEmpty())
			{
				setStackInSlot(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
			}
			else
			{
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
		}

		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		if (existing.isEmpty())
			return ItemStack.EMPTY;

		int toExtract = Math.min(amount, existing.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));

		if (existing.getCount() <= toExtract)
		{
			if (!simulate)
			{
				setStackInSlot(slot, ItemStack.EMPTY);
				return existing;
			}
			else
			{
				return existing.copy();
			}
		}
		else
		{
			if (!simulate)
			{
				setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
			}

			return existing.copyWithCount(toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return inventory.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return inventory.canPlaceItem(slot, resource.toStack(count));
	}

	private void validateSlotIndex(int slot)
	{
		if (slot < 0 || slot >= getSlots())
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
	}

	private int getStackLimit(int slot, ItemStack stack)
	{
		return Math.min(getSlotLimit(slot), stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
	}*/
}
