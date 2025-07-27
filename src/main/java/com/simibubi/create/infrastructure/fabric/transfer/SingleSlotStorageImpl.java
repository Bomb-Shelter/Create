package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class SingleSlotStorageImpl<V, T extends TransferVariant<V>> implements SingleSlotStorage<T> {
	private final T variant;
	private final long amount;
	private final long capacity;

	public SingleSlotStorageImpl(T variant, long amount, long capacity) {
		this.variant = variant;
		this.amount = amount;
		this.capacity = capacity;
	}

	@Override
	public long insert(T resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return variant.isBlank();
	}

	@Override
	public T getResource() {
		return variant;
	}

	@Override
	public long getAmount() {
		return amount;
	}

	@Override
	public long getCapacity() {
		return capacity;
	}
}
