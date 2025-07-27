package com.simibubi.create.infrastructure.fabric.transfer.wrapper;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public interface IItemHandlerModifiable extends SlottedStackStorage {
	@Override
	default SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return new SingleSlotStorage<>() {
			@Override
			public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
				return insertSlot(slot, resource, maxAmount, transaction);
			}

			@Override
			public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
				return extractSlot(slot, resource, maxAmount, transaction);
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
				return getSlotLimit(slot);
			}
		};
	}

	@Override
	default long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int limitedMaxAmount = TransferUtil.truncateLong(maxAmount);
		for (int i = 0; i < getSlotCount(); i++) {
			if (isItemValid(i, resource, limitedMaxAmount)) {
				ItemStack stack = resource.toStack(limitedMaxAmount);
				var inserted = insertItem(i, stack, true);
				int slot = i;
				new FinalCommitSnapshot(maxAmount, () -> {
					insertItem(slot, stack, false);
				}).updateSnapshots(transaction);

				return maxAmount - inserted.getCount();
			}
		}

		return 0;
	}

	@Override
	default long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		var inserted = insertItem(slot, resource.toStack(TransferUtil.truncateLong(maxAmount)), true);

		new FinalCommitSnapshot(maxAmount, () -> insertItem(slot, resource.toStack(TransferUtil.truncateLong(maxAmount)), false))
			.updateSnapshots(transaction);

		return maxAmount - inserted.getCount();
	}

	@Override
	default long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		var extracted = extractItem(slot, TransferUtil.truncateLong(maxAmount), true);

		new FinalCommitSnapshot(maxAmount, () -> extractItem(slot, TransferUtil.truncateLong(maxAmount), false))
			.updateSnapshots(transaction);

		return extracted.getCount();
	}

	@Override
	default long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int limitedMaxAmount = TransferUtil.truncateLong(maxAmount);
		for (int i = 0; i < getSlotCount(); i++) {
			if (isItemValid(i, resource, limitedMaxAmount)) {
				var extracted = extractItem(i, limitedMaxAmount, true);
				int slot = i;
				new FinalCommitSnapshot(maxAmount, () -> {
					extractItem(slot, limitedMaxAmount, false);
				}).updateSnapshots(transaction);

				return extracted.getCount();
			}
		}

		return 0;
	}

	ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
	ItemStack extractItem(int slot, int amount, boolean simulate);

	@Override
	default boolean isItemValid(int slot, ItemVariant resource, int count) {
		return isItemValid(slot, resource.toStack(count));
	}

	boolean isItemValid(int slot, ItemStack stack);
}
