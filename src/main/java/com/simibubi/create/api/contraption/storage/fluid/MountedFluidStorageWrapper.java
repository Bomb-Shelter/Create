package com.simibubi.create.api.contraption.storage.fluid;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.core.BlockPos;

/**
 * Wrapper around many MountedFluidStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 */
public class MountedFluidStorageWrapper extends CombinedTankWrapper {
	public final ImmutableMap<BlockPos, MountedFluidStorage> storages;

	public MountedFluidStorageWrapper(ImmutableMap<BlockPos, MountedFluidStorage> storages) {
		super(storages.values().toArray(SlottedStorage[]::new));
		this.storages = storages;
	}
}
