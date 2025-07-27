package com.simibubi.create.content.fluids.hosePulley;

import java.util.Iterator;
import java.util.function.Supplier;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class HosePulleyFluidHandler implements SlottedStorage<FluidVariant> {

	// The dynamic interface

	private class HosePulleyInsertSnapshot extends SnapshotParticipant<Long> {
		private final FluidVariant variant;
		private long amount;

		public HosePulleyInsertSnapshot(FluidVariant variant, long amount) {
			this.variant = variant;
			this.amount = amount;
		}

		@Override
		protected Long createSnapshot() {
			return amount;
		}

		@Override
		protected void readSnapshot(Long snapshot) {
			amount = snapshot;
		}

		@Override
		protected void onFinalCommit() {
			filler.tryDeposit(variant.getFluid(), rootPosGetter.get(), false);
		}
	}

	private class HosePulleyExtractSnapshot extends SnapshotParticipant<Long> {
		public FluidStack leftover;
		private long amount;

		public HosePulleyExtractSnapshot(long amount) {
			this.amount = amount;
		}

		@Override
		protected Long createSnapshot() {
			return amount;
		}

		@Override
		protected void readSnapshot(Long snapshot) {
			this.amount = snapshot;
		}

		@Override
		protected void onFinalCommit() {
			drainer.pullNext(rootPosGetter.get(), false);

			if (leftover != null && !leftover.isEmpty()) {
				internalTank.setFluid(leftover);
			}
		}
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (!internalTank.isEmpty() && !FluidStack.isSameFluidSameComponents(new FluidStack(resource, maxAmount), internalTank.getFluid()))
			return 0;
		if (resource.isBlank() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		var snapshot = new HosePulleyInsertSnapshot(resource, maxAmount);
		snapshot.updateSnapshots(transaction);

		long diff = maxAmount;
		long remaining = maxAmount;
		long totalAmountAfterFill = diff + internalTank.getFluidAmount();
		boolean deposited = false;

		if (predicate.get() && totalAmountAfterFill >= 1000) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), true)) {
				drainer.counterpartActed();
				remaining -= (1000);
				diff -= 1000;
				deposited = true;
			}
		}

		//if (action.simulate())
			//return diff <= 0 ? resource.getAmount() : internalTank.fill(remaining, action);
		if (diff <= 0) {
			return internalTank.extract(resource, -diff, transaction);
		}

		return internalTank.insert(resource, remaining, transaction) + (deposited ? 1000 : 0);
	}

	@Override
	public SingleSlotStorage<FluidVariant> getSlot(int slot) {
		if (internalTank.isEmpty())
			return drainer.getDrainableFluidView(rootPosGetter.get());

		return internalTank.getSlot(slot);
	}

	@Override
	public long extract(FluidVariant resource, long maxDrain, TransactionContext transaction) {
		if (resource != null && !internalTank.isEmpty() && !FluidStack.isSameFluidSameComponents(new FluidStack(resource, maxDrain), internalTank.getFluid()))
			return 0;
		if (internalTank.getFluidAmount() >= 1000)
			return internalTank.extract(resource, maxDrain, transaction);

		var snapshot = new HosePulleyExtractSnapshot(maxDrain);
		snapshot.updateSnapshots(transaction);
		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);
		if (!predicate.get() || !drainer.pullNext(pos, true))
			return internalTank.extract(internalTank.getResource(), maxDrain, transaction);

		filler.counterpartActed();
		FluidStack leftover = returned.copy();
		long available = 1000 + internalTank.getFluidAmount();
		long drained;

		if (!internalTank.isEmpty() && !FluidStack.isSameFluidSameComponents(internalTank.getFluid(), returned)
				|| returned.isEmpty())
			return internalTank.extract(internalTank.getResource(), maxDrain, transaction);

		if (resource != null && !FluidStack.isSameFluidSameComponents(returned, new FluidStack(resource, maxDrain)))
			return 0;

		drained = Math.min(maxDrain, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		snapshot.leftover = leftover;
		return returned.getAmount();
	}

	//

	private SmartFluidTank internalTank;
	private FluidFillingBehaviour filler;
	private FluidDrainingBehaviour drainer;
	private Supplier<BlockPos> rootPosGetter;
	private Supplier<Boolean> predicate;

	public HosePulleyFluidHandler(SmartFluidTank internalTank, FluidFillingBehaviour filler,
		FluidDrainingBehaviour drainer, Supplier<BlockPos> rootPosGetter, Supplier<Boolean> predicate) {
		this.internalTank = internalTank;
		this.filler = filler;
		this.drainer = drainer;
		this.rootPosGetter = rootPosGetter;
		this.predicate = predicate;
	}

	@Override
	public int getSlotCount() {
		return internalTank.getSlotCount();
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		return internalTank.iterator();
	}

	public SmartFluidTank getInternalTank() {
		return internalTank;
	}

}
