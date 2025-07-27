package com.simibubi.create.content.logistics.crate;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ItemStackHandler {

	private Supplier<ItemStack> suppliedItemStack;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
	}

	@Override
	public int getSlotCount() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = suppliedItemStack.get();
		if (slot == 1)
			return ItemStack.EMPTY;
		if (stack == null)
			return ItemStack.EMPTY;
		if (!stack.isEmpty())
			return stack.copyWithCount(stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
		return stack;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {}

	@Override
	public boolean supportsInsertion() {
		return false;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack stack = suppliedItemStack.get();
		if (slot == 1)
			return 0;
		if (stack == null)
			return 0;
		if (!stack.isEmpty())
			return stack.copyWithCount((int) Math.min(stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64), maxAmount)).getCount();
		return 0;
	}

}
