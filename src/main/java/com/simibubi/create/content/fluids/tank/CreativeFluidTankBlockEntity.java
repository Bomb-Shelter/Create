package com.simibubi.create.content.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeFluidTankBlockEntity extends FluidTankBlockEntity {

	public CreativeFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities() {
		FluidStorage.SIDED.registerForBlockEntity((be, context) -> {
			if (be.fluidCapability == null)
				be.refreshCapability();
			return be.fluidCapability;
		}, AllBlockEntityTypes.CREATIVE_FLUID_TANK.get());
	}

	@Override
	protected SmartFluidTank createInventory() {
		return new CreativeSmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	public static class CreativeSmartFluidTank extends SmartFluidTank {
		public static final Codec<CreativeSmartFluidTank> CODEC = RecordCodecBuilder.create(i -> i.group(
			FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(FluidTank::getFluid),
			Codec.LONG.fieldOf("capacity").forGetter(FluidTank::getCapacity)
		).apply(i, (fluid, capacity) -> {
			CreativeSmartFluidTank tank = new CreativeSmartFluidTank(capacity, $ -> {});
			tank.setFluid(fluid);
			return tank;
		}));

		public CreativeSmartFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
			super(capacity, updateCallback);
		}

		@Override
		public long getFluidAmount() {
			return getFluid().isEmpty() ? 0 : getSlot(0).getCapacity();
		}

		public void setContainedFluid(FluidStack fluidStack) {
			setFluid(fluidStack.copy());
			if (!fluidStack.isEmpty())
				getFluid().setAmount(getSlot(0).getCapacity());
			onContentsChanged();
		}

		@Override
		public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
			try (Transaction transaction1 = Transaction.openNested(transaction)) {
				return super.extract(extractedVariant, maxAmount, transaction1);
			}
		}

	}

}
