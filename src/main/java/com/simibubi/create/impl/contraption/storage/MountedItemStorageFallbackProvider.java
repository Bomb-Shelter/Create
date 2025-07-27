package com.simibubi.create.impl.contraption.storage;

import io.github.fabricators_of_create.porting_lib.event.common.TagsUpdatedEvent;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.world.level.block.Block;

public enum MountedItemStorageFallbackProvider implements SimpleRegistry.Provider<Block, MountedItemStorageType<?>> {
	INSTANCE;

	@Override
	@Nullable
	public MountedItemStorageType<?> get(Block block) {
		return AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(block)
			? null
			: AllMountedStorageTypes.FALLBACK.get();
	}

	@Override
	public void onRegister(Runnable invalidate) {
		TagsUpdatedEvent.EVENT.register(event -> {
			if (event.shouldUpdateStaticData()) {
				invalidate.run();
			}
		});
	}
}
