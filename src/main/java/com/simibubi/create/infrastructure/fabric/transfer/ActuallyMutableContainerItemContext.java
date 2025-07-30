package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public class ActuallyMutableContainerItemContext implements ContainerItemContext {
	private final Slot slot;

	public ActuallyMutableContainerItemContext(ItemStack initial) {
		this.slot = new Slot(initial);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getMainSlot() {
		return this.slot;
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		return 0;
	}

	public ItemStack getContainer() {
		return slot.getStack();
	}

	@Override
	@UnmodifiableView
	public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
		return List.of();
	}

	private static class Slot extends SingleStackStorage {
		private ItemStack stack;

		public Slot(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		protected ItemStack getStack() {
			return stack;
		}

		@Override
		protected void setStack(ItemStack stack) {
			this.stack = stack;
		}
	}
}
