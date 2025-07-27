package com.simibubi.create.content.processing.basin;

import com.simibubi.create.foundation.item.SmartInventory;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class BasinInventory extends SmartInventory {

	private BasinBlockEntity blockEntity;

	public boolean packagerMode;

	public BasinInventory(int slots, BasinBlockEntity be) {
		super(slots, be, 64, true);
		this.blockEntity = be;
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (packagerMode) // Unique stack insertion only matters for belt setups
			return inv.insertSlot(slot, resource, maxAmount, transaction);

		int firstFreeSlot = -1;

		for (int i = 0; i < getSlotCount(); i++) {
			// Only insert if no other slot already has a stack of this item
			if (i != slot && resource.isOf(inv.getStackInSlot(i).getItem()) && resource.componentsMatch(inv.getStackInSlot(i).getComponentsPatch()))
				return 0;
			if (inv.getStackInSlot(i)
				.isEmpty() && firstFreeSlot == -1)
				firstFreeSlot = i;
		}

		// Only insert if this is the first empty slot, prevents overfilling in the
		// simulation pass
		if (inv.getStackInSlot(slot)
			.isEmpty() && firstFreeSlot != slot)
			return 0;

		return super.insertSlot(slot, resource, maxAmount, transaction);
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = super.extractSlot(slot, resource, maxAmount, transaction);
		new FinalCommitSnapshot(maxAmount, () -> {
			if (extracted > 0)
				blockEntity.notifyChangeOfContents();
		}).updateSnapshots(transaction);
		return extracted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = super.extract(resource, maxAmount, transaction);
		new FinalCommitSnapshot(maxAmount, () -> {
			if (extracted > 0)
				blockEntity.notifyChangeOfContents();
		}).updateSnapshots(transaction);
		return extracted;
	}

}
