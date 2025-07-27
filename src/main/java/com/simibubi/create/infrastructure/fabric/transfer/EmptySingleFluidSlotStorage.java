package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class EmptySingleFluidSlotStorage implements SingleSlotStorage<FluidVariant> {
	public static final EmptySingleFluidSlotStorage INSTANCE = new EmptySingleFluidSlotStorage(0L);

	private final long capacity;

	public EmptySingleFluidSlotStorage(long capacity) {
		this.capacity = capacity;
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return true;
	}

	@Override
	public FluidVariant getResource() {
		return FluidVariant.blank();
	}

	@Override
	public long getAmount() {
		return 0;
	}

	@Override
	public long getCapacity() {
		return capacity;
	}
}
