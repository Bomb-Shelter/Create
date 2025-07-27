package com.simibubi.create.foundation.fluid;

import com.simibubi.create.infrastructure.fabric.transfer.EmptySingleFluidSlotStorage;

import net.createmod.catnip.data.Iterate;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

/**
 * Combines multiple IFluidHandlers into one interface (See CombinedInvWrapper
 * for items)
 */
public class CombinedTankWrapper implements SlottedStorage<FluidVariant> {

	protected final SlottedStorage<FluidVariant>[] itemHandler;
	protected final int[] baseIndex;
	protected final int tankCount;
	protected boolean enforceVariety;

	public CombinedTankWrapper(SlottedStorage<FluidVariant>... fluidHandlers) {
		this.itemHandler = fluidHandlers;
		this.baseIndex = new int[fluidHandlers.length];
		int index = 0;
		for (int i = 0; i < fluidHandlers.length; i++) {
			index += fluidHandlers[i].getSlotCount();
			baseIndex[i] = index;
		}
		this.tankCount = index;
	}

	public CombinedTankWrapper enforceVariety() {
		enforceVariety = true;
		return this;
	}

	@Override
	public int getSlotCount() {
		return tankCount;
	}

	@Override
	public SingleSlotStorage<FluidVariant> getSlot(int tank) {
		int index = getIndexForSlot(tank);
		SlottedStorage<FluidVariant> handler = getHandlerFromIndex(index);
		tank = getSlotFromIndex(tank, index);
		return handler.getSlot(tank);
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (resource.isBlank())
			return 0;

		long filled = 0;

		boolean fittingHandlerFound = false;
		Outer: for (boolean searchPass : Iterate.trueAndFalse) {
			for (SlottedStorage<FluidVariant> iFluidHandler : itemHandler) {
				for (int i = 0; i < iFluidHandler.getSlotCount(); i++)
					if (searchPass && iFluidHandler.getSlot(i).getResource().equals(resource))
						fittingHandlerFound = true;

				if (searchPass && !fittingHandlerFound)
					continue;

				long filledIntoCurrent = iFluidHandler.insert(resource, maxAmount, transaction);
				filled += filledIntoCurrent;

				if (filled >= maxAmount)
					break Outer;
				if (fittingHandlerFound && (enforceVariety || filledIntoCurrent != 0))
					break Outer;
			}
		}

		return filled;
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (resource.isBlank())
			return 0;

		long drained = 0;

		for (SlottedStorage<FluidVariant> view : itemHandler) {
			long amount = view.extract(resource, maxAmount, transaction);
			drained += amount;

			if (drained >= maxAmount)
				break;
		}

		return drained;
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		return null;
	}

	protected int getIndexForSlot(int slot) {
		if (slot < 0)
			return -1;
		for (int i = 0; i < baseIndex.length; i++)
			if (slot - baseIndex[i] < 0)
				return i;
		return -1;
	}

	protected SlottedStorage<FluidVariant> getHandlerFromIndex(int index) {
		if (index < 0 || index >= itemHandler.length)
			return new EmptySingleFluidSlotStorage(0);
		return itemHandler[index];
	}

	protected int getSlotFromIndex(int slot, int index) {
		if (index <= 0 || index >= baseIndex.length)
			return slot;
		return slot - baseIndex[index - 1];
	}
}
