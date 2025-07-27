package com.simibubi.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;

import com.simibubi.create.infrastructure.fabric.transfer.CombinedInventoryStorage;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.minecraft.core.BlockPos;

/**
 * Wrapper around many MountedItemStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 */
public class MountedItemStorageWrapper extends CombinedInventoryStorage {
	public final ImmutableMap<BlockPos, MountedItemStorage> storages;

	public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
		super(storages.values().toArray(SlottedStackStorage[]::new));
		this.storages = storages;
	}
}
