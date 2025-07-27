package com.simibubi.create.content.kinetics.belt.transport;

import com.simibubi.create.foundation.item.ItemHelper;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class ItemHandlerBeltSegment implements SlottedStackStorage {

	private final BeltInventory beltInventory;
	private final BeltSegmentSingleSlot segmentSlot = new BeltSegmentSingleSlot();
	int offset;

	public ItemHandlerBeltSegment(BeltInventory beltInventory, int offset) {
		this.beltInventory = beltInventory;
		this.offset = offset;
	}

	@Override
	public int getSlotCount() {
		return 1;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return segmentSlot;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		TransportedItemStack stackAtOffset = this.beltInventory.getStackAtOffset(offset);
		if (stackAtOffset == null)
			return ItemStack.EMPTY;
		return stackAtOffset.stack;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		TransportedItemStack stackAtOffset = this.beltInventory.getStackAtOffset(offset);

		if (stackAtOffset == null)
			return;

		stackAtOffset.stack = stack;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getStackInSlot(slot).getOrDefault(DataComponents.MAX_STACK_SIZE, 64), 64);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return segmentSlot.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return segmentSlot.extract(resource, maxAmount, transaction);
	}

	public class BeltSegmentSingleSlot implements SingleSlotStorage<ItemVariant> {
		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (beltInventory.canInsertAt(offset)) {
				ItemStack stack = CreateTransferUtil.getLimitedStack(resource, maxAmount);
				ItemStack remainder = ItemHelper.limitCountToMaxStackSize(stack, true);

				var snapshot = new FinalCommitSnapshot(maxAmount, () -> {
					TransportedItemStack newStack = new TransportedItemStack(stack);
					newStack.insertedAt = offset;
					newStack.beltPosition = offset + .5f + (beltInventory.beltMovementPositive ? -1 : 1) / 16f;
					newStack.prevBeltPosition = newStack.beltPosition;
					beltInventory.addItem(newStack);
					beltInventory.belt.setChanged();
					beltInventory.belt.sendData();
				});
				snapshot.updateSnapshots(transaction);

				return maxAmount - remainder.getCount();
			}
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			TransportedItemStack transported = beltInventory.getStackAtOffset(offset);
			if (transported == null)
				return 0;

			long amount = Math.min(maxAmount, transported.stack.getCount());
			ItemStack extracted = transported.stack.copy().split((int) amount);

			var snapshot = new FinalCommitSnapshot(amount, () -> {
				transported.stack.split((int) amount);
				if (transported.stack.isEmpty())
					beltInventory.toRemove.add(transported);
				else
					beltInventory.belt.notifyUpdate();
			});

			snapshot.updateSnapshots(transaction);

			return extracted.getCount();
		}

		@Override
		public boolean isResourceBlank() {
			TransportedItemStack transported = beltInventory.getStackAtOffset(offset);

			if (transported == null)
				return true;

			return transported.stack.isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			TransportedItemStack transported = beltInventory.getStackAtOffset(offset);

			if (transported == null)
				return ItemVariant.blank();

			return ItemVariant.of(transported.stack);
		}

		@Override
		public long getAmount() {
			TransportedItemStack transported = beltInventory.getStackAtOffset(offset);

			if (transported == null)
				return 0;

			return transported.stack.getCount();
		}

		@Override
		public long getCapacity() {
			TransportedItemStack transported = beltInventory.getStackAtOffset(offset);

			if (transported == null)
				return 0;

			return CreateTransferUtil.getMaxStackSize(transported.stack);
		}
	}
}
