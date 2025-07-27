package com.simibubi.create.api.contraption.storage.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.NotNull;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

import java.util.Iterator;

/**
 * Partial implementation of a MountedFluidStorage that wraps a fluid handler.
 */
public abstract class WrapperMountedFluidStorage<T extends SlottedStorage<FluidVariant>> extends MountedFluidStorage {
	protected final T wrapped;

	protected WrapperMountedFluidStorage(MountedFluidStorageType<?> type, T wrapped) {
		super(type);
		this.wrapped = wrapped;
	}

	@Override
	public int getSlotCount() {
		return this.wrapped.getSlotCount();
	}

	@Override
	public SingleSlotStorage<FluidVariant> getSlot(int slot) {
		return this.wrapped.getSlot(slot);
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		return this.wrapped.iterator();
	}
}
