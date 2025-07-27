package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ExtractOnlySingleSlotStorage<T> implements SingleSlotStorage<T> {
	private final SingleSlotStorage<T> wrapped;

	public ExtractOnlySingleSlotStorage(SingleSlotStorage<T> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public long insert(T resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public boolean isResourceBlank() {
		return this.wrapped.isResourceBlank();
	}

	@Override
	public T getResource() {
		return this.wrapped.getResource();
	}

	@Override
	public long getAmount() {
		return this.wrapped.getAmount();
	}

	@Override
	public long getCapacity() {
		return this.wrapped.getCapacity();
	}
}
