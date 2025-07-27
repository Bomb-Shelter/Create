package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class DepotItemHandler implements SlottedStackStorage {

	private static final int MAIN_SLOT = 0;
	private DepotBehaviour behaviour;

	public DepotItemHandler(DepotBehaviour behaviour) {
		this.behaviour = behaviour;
	}

	@Override
	public int getSlotCount() {
		return 9;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return new DepotSingleSlot(slot);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot == MAIN_SLOT ? behaviour.getHeldItemStack() : behaviour.processingOutputBuffer.getStackInSlot(slot - 1);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (slot == MAIN_SLOT)
			behaviour.setHeldItem(new TransportedItemStack(stack));
		else
			behaviour.processingOutputBuffer.setStackInSlot(slot - 1, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return slot == MAIN_SLOT ? behaviour.maxStackSize.get() : 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int amount) {
		return slot == MAIN_SLOT && behaviour.isItemValid(resource.toStack(amount));
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	public class DepotSingleSlot implements SingleSlotStorage<ItemVariant> {
		private int slot;

		public DepotSingleSlot(int slot) {
			this.slot = slot;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (slot != MAIN_SLOT)
				return 0;
			if (!behaviour.getHeldItemStack()
				.isEmpty() && !behaviour.canMergeItems())
				return 0;
			if (!behaviour.isOutputEmpty() && !behaviour.canMergeItems())
				return 0;

			ItemStack stack = CreateTransferUtil.getLimitedStack(resource, maxAmount);
			ItemStack remainder = behaviour.insert(new TransportedItemStack(stack), true);
			new FinalCommitSnapshot(maxAmount, () -> {
				behaviour.insert(new TransportedItemStack(stack), false);
				if (remainder != stack)
					behaviour.blockEntity.notifyUpdate();
			}).updateSnapshots(transaction);
			return stack.getCount() - remainder.getCount();
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (slot != MAIN_SLOT)
				return behaviour.processingOutputBuffer.extractSlot(slot - 1, resource, maxAmount, transaction);

			TransportedItemStack held = behaviour.heldItem;
			if (held == null)
				return 0;
			ItemStack stack = held.stack.copy();
			ItemStack extracted = stack.split((int) Math.min(CreateTransferUtil.getMaxStackSize(resource), maxAmount));
			new FinalCommitSnapshot(maxAmount, () -> {
				behaviour.heldItem.stack = stack;
				if (stack.isEmpty())
					behaviour.heldItem = null;
				behaviour.blockEntity.notifyUpdate();
			}).updateSnapshots(transaction);
			return extracted.getCount();
		}

		@Override
		public boolean isResourceBlank() {
			return getStackInSlot(slot).isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(getStackInSlot(slot));
		}

		@Override
		public long getAmount() {
			return getStackInSlot(slot).getCount();
		}

		@Override
		public long getCapacity() {
			return CreateTransferUtil.getMaxStackSize(getStackInSlot(slot));
		}
	}
}
