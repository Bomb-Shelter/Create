package com.simibubi.create.content.logistics.crate;

import java.util.List;
import java.util.SortedSet;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerSlot;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler implements SlottedStackStorage {

	private Supplier<ItemStack> suppliedItemStack;
	private List<BottomlessSlot> slots;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
		ImmutableList.Builder<BottomlessSlot> builder = ImmutableList.builder();
		for (int i = 0; i < getSlotCount(); i++) {
			builder.add(new BottomlessSlot(i));
		}
		this.slots = builder.build();
	}

	@Override
	public int getSlotCount() {
		return 2;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return slots.get(slot);
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
	public int getSlotLimit(int slot) {
		return Item.ABSOLUTE_MAX_STACK_SIZE;
	}

	@Override
	public boolean supportsInsertion() {
		return false;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (slots.isEmpty())
			return 0; // no slots hold this item
		long extracted = 0;
		for (BottomlessSlot slot : slots) {
			extracted += slot.extract(resource, maxAmount - extracted, transaction);
			if (extracted >= maxAmount)
				break;
		}
		return extracted;
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).extract(resource, maxAmount, transaction);
	}

	public class BottomlessSlot implements SingleSlotStorage<ItemVariant> {
		private final int slot;

		public BottomlessSlot(int slot) {
			this.slot = slot;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			ItemStack stack = suppliedItemStack.get();
			if (slot == 1)
				return 0;
			if (stack == null)
				return 0;
			if (!stack.isEmpty())
				return Math.min(stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64), maxAmount);
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			return slot == 1;
		}

		@Override
		public ItemVariant getResource() {
			ItemStack stack = suppliedItemStack.get();
			if (slot == 1 || stack == null)
				return ItemVariant.blank();
			if (!stack.isEmpty())
				return ItemVariant.of(stack);
			return ItemVariant.blank();
		}

		@Override
		public long getAmount() {
			ItemStack stack = suppliedItemStack.get();
			if (slot == 1)
				return 0;
			if (stack == null)
				return 0;
			if (!stack.isEmpty())
				return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
			return 0;
		}

		@Override
		public long getCapacity() {
			return Item.ABSOLUTE_MAX_STACK_SIZE;
		}
	}
}
