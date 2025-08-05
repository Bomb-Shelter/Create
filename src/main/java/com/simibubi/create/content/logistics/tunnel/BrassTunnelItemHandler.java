package com.simibubi.create.content.logistics.tunnel;

import com.simibubi.create.foundation.item.ItemHelper;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class BrassTunnelItemHandler implements SlottedStorage<ItemVariant> {

	private BrassTunnelBlockEntity blockEntity;
	private final BrassTunnelSingleSlot tunnelSingleSlot = new BrassTunnelSingleSlot();

	public BrassTunnelItemHandler(BrassTunnelBlockEntity be) {
		this.blockEntity = be;
	}

	@Override
	public int getSlotCount() {
		return 1;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return tunnelSingleSlot;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return tunnelSingleSlot.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return tunnelSingleSlot.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return tunnelSingleSlot.iterator();
	}

	public class BrassTunnelSingleSlot implements SingleSlotStorage<ItemVariant> {
		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!blockEntity.hasDistributionBehaviour()) {
				Storage<ItemVariant> beltCapability = blockEntity.getBeltCapability();
				if (beltCapability == null)
					return 0;
				return beltCapability.insert(resource, maxAmount, transaction);
			}

			if (!blockEntity.canTakeItems())
				return 0;

			ItemStack stack = CreateTransferUtil.getLimitedStack(resource, maxAmount);
			ItemStack remainder = ItemHelper.limitCountToMaxStackSize(stack, true);
			var snapshot = new FinalCommitSnapshot(maxAmount, () -> {
				ItemHelper.limitCountToMaxStackSize(stack, true);
				blockEntity.setStackToDistribute(stack, null);
			});
			snapshot.updateSnapshots(transaction);

			return CreateTransferUtil.getMaxStackSize(resource) - remainder.getCount();
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			Storage<ItemVariant> beltCapability = blockEntity.getBeltCapability();
			if (beltCapability == null)
				return 0L;
			return beltCapability.extract(resource, maxAmount, transaction);
		}

		@Override
		public boolean isResourceBlank() {
			return blockEntity.stackToDistribute.isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(blockEntity.stackToDistribute);
		}

		@Override
		public long getAmount() {
			return blockEntity.stackToDistribute.getCount();
		}

		@Override
		public long getCapacity() {
			return blockEntity.stackToDistribute.isEmpty() ? 64 : blockEntity.stackToDistribute.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
		}
	}
}
