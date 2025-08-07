package com.simibubi.create.content.fluids.hosePulley;

import java.util.Iterator;
import java.util.function.Supplier;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class HosePulleyFluidHandler implements SingleSlotStorage<FluidVariant> {

	// The dynamic interface

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (!internalTank.isEmpty() && !FluidStack.isSameFluidSameComponents(new FluidStack(resource, maxAmount), internalTank.getFluid()))
			return 0;
		if (resource.isBlank() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		long diff = maxAmount;
		long remaining = maxAmount;
		long totalAmountAfterFill = diff + internalTank.getFluidAmount();
		boolean deposited = false;

		if (predicate.get() && totalAmountAfterFill >= FluidConstants.BLOCK) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), true)) {
				drainer.counterpartActed();
				remaining -= (FluidConstants.BLOCK);
				diff -= FluidConstants.BLOCK;
				deposited = true;
			}
		}

		if (diff <= 0) {
			internalTank.extract(resource, -diff, transaction);
			return maxAmount;
		}

		return internalTank.insert(resource, remaining, transaction) + (deposited ? FluidConstants.BLOCK : 0);
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
		if (internalTank.getFluidAmount() >= FluidConstants.BLOCK)
			return internalTank.extract(resource, maxDrain, transaction);

		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);

		boolean predicateValue = predicate.get();
		if (!predicateValue || !drainer.pullNext(pos, true)) {
			if (predicateValue) {
				new FinalCommitSnapshot(maxDrain, () -> {
					drainer.pullNext(pos, false);
				})
					.updateSnapshots(transaction);
			}

			return internalTank.extract(resource, maxDrain, transaction);
		}

		filler.counterpartActed();
		FluidStack leftover = returned.copy();
		long available = FluidConstants.BLOCK + internalTank.getFluidAmount();
		long drained;

		if (!internalTank.isEmpty() && !FluidStack.isSameFluidSameComponents(internalTank.getFluid(), returned)
				|| returned.isEmpty())
			return internalTank.extract(resource, maxDrain, transaction);

		if (resource != null && !FluidStack.isSameFluidSameComponents(returned, new FluidStack(resource, maxDrain)))
			return 0;

		drained = Math.min(maxDrain, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		(new FinalCommitSnapshot(maxDrain, () -> {
			if (!leftover.isEmpty())
				internalTank.setFluid(leftover);
		}))
			.updateSnapshots(transaction);
		return returned.getAmount();
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		if (!internalTank.isResourceBlank() || drainer.blockEntity.getLevel() == null) return internalTank.getResource();
		FluidState state = drainer.blockEntity.getLevel().getFluidState(rootPosGetter.get());
		Fluid f = state.getType();
		if (f instanceof FlowingFluid flowing) f = flowing.getSource();
		if (!f.isSource(state)) return FluidVariant.blank();
		return FluidVariant.of(f);
	}

	@Override
	public long getAmount() {
		return isResourceBlank() ? 0 : Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
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

	public SmartFluidTank getInternalTank() {
		return internalTank;
	}

}
