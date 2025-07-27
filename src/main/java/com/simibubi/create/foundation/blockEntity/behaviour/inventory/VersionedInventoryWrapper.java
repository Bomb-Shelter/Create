package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class VersionedInventoryWrapper implements SlottedStackStorage {

	public static final AtomicInteger idGenerator = new AtomicInteger();

	private SlottedStackStorage inventory;
	private int version;
	private int id;

	public VersionedInventoryWrapper(SlottedStackStorage inventory) {
		this.id = idGenerator.getAndIncrement();
		this.inventory = inventory;
		this.version = 0;
	}

	public void incrementVersion() {
		version++;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long result = inventory.insert(resource, maxAmount, transaction);

		(new FinalCommitSnapshot(maxAmount, () -> {
			if (maxAmount != result)
				incrementVersion();
		})).updateSnapshots(transaction);

		return result;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long result = inventory.extract(resource, maxAmount, transaction);

		(new FinalCommitSnapshot(maxAmount, () -> {
			if (result > 0)
				incrementVersion();
		})).updateSnapshots(transaction);

		return result;
	}

	public long getVersion() {
		return version;
	}

	public int getId() {
		return id;
	}

	//

	@Override
	public int getSlotCount() {
		return inventory.getSlotCount();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return inventory.getSlot(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inventory.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return inventory.isItemValid(slot, resource, count);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.getStackInSlot(slot);
	}

	//


	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long result = inventory.insertSlot(slot, resource, maxAmount, transaction);

		(new FinalCommitSnapshot(maxAmount, () -> {
			if (maxAmount != result)
				incrementVersion();
		})).updateSnapshots(transaction);

		return result;
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long result = inventory.extractSlot(slot, resource, maxAmount, transaction);

		(new FinalCommitSnapshot(maxAmount, () -> {
			if (result > 0)
				incrementVersion();
		})).updateSnapshots(transaction);

		return result;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		ItemStack previousItem = inventory.getStackInSlot(slot);
		inventory.setStackInSlot(slot, stack);

		if (stack.isEmpty() == previousItem.isEmpty()) {
			if (stack.isEmpty())
				return;
			if (ItemStack.isSameItemSameComponents(stack, previousItem)
				&& stack.getCount() == previousItem.getCount())
				return;
		}

		incrementVersion();
	}

}
