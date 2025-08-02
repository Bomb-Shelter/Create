package com.simibubi.create.api.contraption.storage.item.chest;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import com.simibubi.create.infrastructure.fabric.transfer.InventoryStorage;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class ChestMountedStorageType extends SimpleMountedStorageType<ChestMountedStorage> {
	public ChestMountedStorageType() {
		super(ChestMountedStorage.CODEC);
	}

	@Override
	protected SlottedStorage<ItemVariant> getStorage(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof Container container ? InventoryStorage.of(container, null) : null;
	}

	@Override
	protected @Nullable SimpleMountedStorage createStorage(SlottedStorage<ItemVariant> storage) {
		return new ChestMountedStorage(storage);
	}
}
