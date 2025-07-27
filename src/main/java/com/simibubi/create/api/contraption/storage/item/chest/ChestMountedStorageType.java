package com.simibubi.create.api.contraption.storage.item.chest;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import com.simibubi.create.infrastructure.fabric.transfer.InventoryStorage;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChestMountedStorageType extends SimpleMountedStorageType<ChestMountedStorage> {
	public ChestMountedStorageType() {
		super(ChestMountedStorage.CODEC);
	}

	@Override
	protected SlottedStackStorage getHandler(Level level, BlockEntity be) {
		return be instanceof Container container ? InventoryStorage.of(container, null) : null;
	}

	@Override
	protected SimpleMountedStorage createStorage(SlottedStackStorage handler) {
		return new ChestMountedStorage(handler);
	}
}
