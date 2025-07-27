package com.simibubi.create.impl.contraption.storage;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@ApiStatus.Internal
public class FallbackMountedStorageType extends SimpleMountedStorageType<FallbackMountedStorage> {
	public FallbackMountedStorageType() {
		super(FallbackMountedStorage.CODEC);
	}

	@Override
	protected SlottedStackStorage getHandler(Level level, BlockEntity be) {
		SlottedStackStorage handler = super.getHandler(level, be);
		return handler != null && FallbackMountedStorage.isValid(handler) ? handler : null;
	}
}
