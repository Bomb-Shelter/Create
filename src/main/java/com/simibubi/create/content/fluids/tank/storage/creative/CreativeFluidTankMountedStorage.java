package com.simibubi.create.content.fluids.tank.storage.creative;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class CreativeFluidTankMountedStorage extends WrapperMountedFluidStorage<CreativeSmartFluidTank> {
	public static final MapCodec<CreativeFluidTankMountedStorage> CODEC = CreativeSmartFluidTank.CODEC.xmap(
		CreativeFluidTankMountedStorage::new, storage -> storage.wrapped
	).fieldOf("value");

	protected CreativeFluidTankMountedStorage(MountedFluidStorageType<?> type, CreativeSmartFluidTank tank) {
		super(type, tank);
	}

	protected CreativeFluidTankMountedStorage(CreativeSmartFluidTank tank) {
		this(AllMountedStorageTypes.CREATIVE_FLUID_TANK.get(), tank);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		// no need to do anything, supplied stack can't change while mounted
	}

	public static CreativeFluidTankMountedStorage fromTank(CreativeFluidTankBlockEntity tank) {
		// make an isolated copy
		FluidTank inv = tank.getTankInventory();
		CreativeSmartFluidTank copy = new CreativeSmartFluidTank(inv.getCapacity(), $ -> {});
		copy.setContainedFluid(inv.getFluid());
		return new CreativeFluidTankMountedStorage(copy);
	}

	public static CreativeFluidTankMountedStorage fromLegacy(HolderLookup.Provider registries, CompoundTag nbt) {
		long capacity;
		if (nbt.contains("Capacity", Tag.TAG_INT))
			capacity = (long) ((nbt.getInt("Capacity") / 1000.0) * FluidConstants.BLOCK);
		else
			capacity = nbt.getLong("Capacity");
		FluidStack fluid = FluidStack.parseOptional(registries, nbt.getCompound("ProvidedStack"));
		CreativeSmartFluidTank tank = new CreativeSmartFluidTank(capacity, $ -> {});
		tank.setContainedFluid(fluid);
		return new CreativeFluidTankMountedStorage(tank);
	}
}
