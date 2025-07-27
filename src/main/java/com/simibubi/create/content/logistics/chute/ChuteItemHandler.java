package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.foundation.item.ItemHelper;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import com.simibubi.create.infrastructure.fabric.transfer.SingleSlotStorageImpl;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class ChuteItemHandler implements SlottedStorage<ItemVariant> {

	private ChuteBlockEntity blockEntity;
	private final ChuteItemSingleSlotStorage singleSlotStorage = new ChuteItemSingleSlotStorage();

	public ChuteItemHandler(ChuteBlockEntity be) {
		this.blockEntity = be;
	}

	@Override
	public int getSlotCount() {
		return 1;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		if (slot != 0)
			throw new IllegalArgumentException();

		return singleSlotStorage;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return singleSlotStorage.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return singleSlotStorage.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return singleSlotStorage.iterator();
	}

	private class ChuteItemSingleSlotStorage implements SingleSlotStorage<ItemVariant> {
		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!blockEntity.canAcceptItem(resource.toStack((int) Math.min(maxAmount, resource.getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, resource.getItem().getDefaultMaxStackSize())))))
				return 0;

			ItemStack stack = CreateTransferUtil.getLimitedStack(resource, maxAmount);
			ItemStack remainder = ItemHelper.limitCountToMaxStackSize(stack, true);
			var snapshot = new FinalCommitSnapshot(maxAmount, () -> {
				ItemHelper.limitCountToMaxStackSize(stack, false);
				blockEntity.setItem(stack);
			});
			snapshot.updateSnapshots(transaction);

			return maxAmount - remainder.getCount();
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			ItemStack remainder = blockEntity.item.copy();
			ItemStack split = remainder.split((int) Math.min(CreateTransferUtil.getMaxStackSize(resource), maxAmount));
			var snapshot = new FinalCommitSnapshot(maxAmount, () -> {
				blockEntity.setItem(remainder);
			});
			snapshot.updateSnapshots(transaction);

			return split.getCount();
		}

		@Override
		public boolean isResourceBlank() {
			return blockEntity.item.isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(blockEntity.item);
		}

		@Override
		public long getAmount() {
			return blockEntity.item.getCount();
		}

		@Override
		public long getCapacity() {
			return Math.min(64, blockEntity.item.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
		}
	}

}
