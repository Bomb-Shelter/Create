package com.simibubi.create.content.logistics.packager;

import com.simibubi.create.content.logistics.box.PackageItem;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class PackagerItemHandler implements SlottedStackStorage {

	private PackagerBlockEntity blockEntity;
	private final PackagerSingleSlotStorage singleSlotStorage = new PackagerSingleSlotStorage();

	public PackagerItemHandler(PackagerBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		if (slot != 0)
			return null;

		return singleSlotStorage;
	}

	@Override
	public int getSlotCount() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return blockEntity.heldBox;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (slot != 0)
			return;
		blockEntity.heldBox = stack;
		blockEntity.notifyUpdate();
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant variant, int count) {
		return PackageItem.isPackage(variant.toStack(count));
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return singleSlotStorage.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return singleSlotStorage.extract(resource, maxAmount, transaction);
	}

	public class PackagerSingleSlotStorage implements SingleSlotStorage<ItemVariant> {
		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!blockEntity.heldBox.isEmpty() || !blockEntity.queuedExitingPackages.isEmpty())
				return 0;
			if (!isItemValid(0, resource, (int) Math.min(maxAmount, CreateTransferUtil.getMaxStackSize(resource))))
				return 0;
			ItemStack stack = CreateTransferUtil.getLimitedStack(resource, maxAmount);
			if (!blockEntity.unwrapBox(stack, true))
				return 0;
			(new FinalCommitSnapshot(maxAmount, () -> {
				blockEntity.unwrapBox(stack, false);
				blockEntity.triggerStockCheck();
			})).updateSnapshots(transaction);
			return 1;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			// Fabric TODO: this doesn't properly handle maxAmount
			if (blockEntity.animationTicks != 0)
				return 0;
			ItemStack box = blockEntity.heldBox;
			(new FinalCommitSnapshot(maxAmount, () -> {
				setStackInSlot(0, ItemStack.EMPTY);
			})).updateSnapshots(transaction);
			return box.getCount();
		}

		@Override
		public boolean isResourceBlank() {
			return blockEntity.heldBox.isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(blockEntity.heldBox);
		}

		@Override
		public long getAmount() {
			return blockEntity.heldBox.getCount();
		}

		@Override
		public long getCapacity() {
			return CreateTransferUtil.getMaxStackSize(blockEntity.heldBox);
		}
	}
}
