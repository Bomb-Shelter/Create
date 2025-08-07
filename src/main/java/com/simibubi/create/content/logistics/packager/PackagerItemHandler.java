package com.simibubi.create.content.logistics.packager;

import com.simibubi.create.content.logistics.box.PackageItem;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class PackagerItemHandler implements SingleSlotStorage<ItemVariant> {

	private final PackagerBlockEntity blockEntity;

	public PackagerItemHandler(PackagerBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (!blockEntity.heldBox.isEmpty() || !blockEntity.queuedExitingPackages.isEmpty())
			return 0;
		if (!PackageItem.isPackage(resource))
			return 0;
		ItemStack stack = resource.toStack(1);
		if (blockEntity.unwrapBox(stack, transaction)) {
			TransactionCallback.onSuccess(transaction, blockEntity::scheduleStockCheck);
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (blockEntity.animationTicks != 0)
			return 0;
		ItemStack box = blockEntity.heldBox;
		if (!resource.matches(box))
			return 0;

		blockEntity.heldBox = ItemStack.EMPTY;
		TransactionCallback.onSuccess(transaction, blockEntity::notifyUpdate);
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
		return 1;
	}
}
