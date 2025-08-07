package com.simibubi.create.compat.tconstruct;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public enum SpoutCasting implements BlockSpoutingBehaviour {
	INSTANCE;

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		if (!enabled())
			return 0;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null)
			return 0;

		Storage<FluidVariant> handler = TransferUtil.getFluidStorage(level, blockEntity.getBlockPos(), blockEntity, Direction.UP);
		if (handler == null)
			return 0;
		if (CreateTransferUtil.getSlotCount(handler) != 1)
			return 0;

		/*if (!handler.isFluidValid(0, availableFluid))
			return 0;*/

		FluidStack containedFluid = TransferUtil.getFirstFluid(handler);
		if (!(containedFluid.isEmpty() || FluidStack.isSameFluidSameComponents(containedFluid, availableFluid)))
			return 0;

		// Do not fill if it would only partially fill the table (unless > 1000mb)
		long amount = availableFluid.getAmount();
		if (amount < FluidConstants.BLOCK
			&& CreateTransferUtil.insertFluid(handler, FluidHelper.copyStackWithAmount(availableFluid, amount + 1), true) > amount)
			return 0;

		// Return amount filled into the table/basin
		return CreateTransferUtil.insertFluid(handler, availableFluid, false);
	}

	private boolean enabled() {
		return AllConfigs.server().recipes.allowCastingBySpout.get();
	}
}
