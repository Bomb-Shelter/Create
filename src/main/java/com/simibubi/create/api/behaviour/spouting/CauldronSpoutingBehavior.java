package com.simibubi.create.api.behaviour.spouting;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

/**
 * {@link BlockSpoutingBehaviour} for empty cauldrons. Mods can register their fluids
 * to {@link #CAULDRON_INFO} to allow spouts to fill empty cauldrons with their fluids.
 */
public enum CauldronSpoutingBehavior implements BlockSpoutingBehaviour {
	INSTANCE;

	public static final SimpleRegistry<Fluid, CauldronInfo> CAULDRON_INFO = Util.make(() -> {
		SimpleRegistry<Fluid, CauldronInfo> registry = SimpleRegistry.create();
		registry.register(Fluids.WATER, new CauldronInfo(CreateTransferUtil.BOTTLE, Blocks.WATER_CAULDRON));
		registry.register(Fluids.LAVA, new CauldronInfo(FluidConstants.BLOCK, Blocks.LAVA_CAULDRON));
		return registry;
	});

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		CauldronInfo info = CAULDRON_INFO.get(availableFluid.getFluid());
		if (info == null)
			return 0;

		if (availableFluid.getAmount() < info.amount)
			return 0;

		if (!simulate) {
			level.setBlockAndUpdate(pos, info.cauldron);
		}

		return info.amount;
	}

	/**
	 * @param amount the amount of fluid that must be inserted into an empty cauldron
	 * @param cauldron the BlockState to set after filling an empty cauldron with the given amount of fluid
	 */
	public record CauldronInfo(long amount, BlockState cauldron) {
		public CauldronInfo(long amount, Block block) {
			this(amount, block.defaultBlockState());
		}
	}
}
