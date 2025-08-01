package com.simibubi.create.impl.contraption.storage;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

/**
 * A fallback mounted storage impl that will try to be used when no type is
 * registered for a block. This requires that the mounted block provide an item handler
 * whose class is exactly {@link ItemStackHandler}.
 */
public class FallbackMountedStorage extends SimpleMountedStorage {
	public static final MapCodec<FallbackMountedStorage> CODEC = SimpleMountedStorage.codec(FallbackMountedStorage::new);

	public FallbackMountedStorage(SlottedStorage<ItemVariant> handler) {
		super(AllMountedStorageTypes.FALLBACK.get(), handler);
	}

	@Override
	protected Optional<SlottedStorage<ItemVariant>> validate(Storage<ItemVariant> handler) {
		return super.validate(handler).filter(FallbackMountedStorage::isValid);
	}

	public static boolean isValid(Storage<ItemVariant> handler) {
		return handler.getClass() == ItemStackHandler.class;
	}
}
