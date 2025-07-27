package com.simibubi.create.infrastructure.fabric.transfer;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;

public class EmptySlottedFluidStorage implements SlottedStorage<FluidVariant> {
	@Override
	public int getSlotCount() {
		return 0;
	}

	@Override
	public SingleSlotStorage<FluidVariant> getSlot(int slot) {
		return EmptySingleFluidSlotStorage.INSTANCE;
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
	public Iterator<StorageView<FluidVariant>> iterator() {
		return Collections.emptyIterator();
	}
}
