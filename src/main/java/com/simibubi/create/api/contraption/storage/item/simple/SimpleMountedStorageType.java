package com.simibubi.create.api.contraption.storage.item.simple;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SimpleMountedStorageType<T extends SimpleMountedStorage> extends MountedItemStorageType<SimpleMountedStorage> {
	protected SimpleMountedStorageType(MapCodec<T> codec) {
		super(codec);
	}

	@Override
	@Nullable
	public SimpleMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return Optional.ofNullable(be)
			.map(b -> getHandler(level, b))
			.map(this::createStorage)
			.orElse(null);
	}

	protected SlottedStackStorage getHandler(Level level, BlockEntity be) {
		Storage<ItemVariant> handler = TransferUtil.getItemStorage(level, be.getBlockPos(), be, null);
		// make sure the handler is modifiable so new contents can be moved over on disassembly
		return handler instanceof SlottedStackStorage modifiable ? modifiable : null;
	}

	protected SimpleMountedStorage createStorage(SlottedStackStorage handler) {
		return new SimpleMountedStorage(this, handler);
	}

	public static final class Impl extends SimpleMountedStorageType<SimpleMountedStorage> {
		public Impl() {
			super(SimpleMountedStorage.CODEC);
		}
	}
}
