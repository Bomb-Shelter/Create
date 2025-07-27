package com.simibubi.create.api.contraption.storage.item.simple;

import java.util.Optional;
import java.util.function.Function;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.simibubi.create.foundation.codec.CreateCodecs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Widely-applicable mounted storage implementation.
 * Gets an item handler from the mounted block, copies it to an ItemStackHandler,
 * and then copies the inventory back to the target when unmounting.
 * All blocks for which this mounted storage is registered must provide a
 * {@link SlottedStackStorage} to {@link ItemStorage#SIDED}.
 * <br>
 * To use this implementation, either register {@link AllMountedStorageTypes#SIMPLE} to your block
 * manually, or add your block to the {@link AllTags.AllBlockTags#SIMPLE_MOUNTED_STORAGE} tag.
 * It is also possible to extend this class to create your own implementation.
 */
public class SimpleMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final MapCodec<SimpleMountedStorage> CODEC = codec(SimpleMountedStorage::new);

	public SimpleMountedStorage(MountedItemStorageType<?> type, SlottedStackStorage handler) {
		super(type, copyToItemStackHandler(handler));
	}

	public SimpleMountedStorage(SlottedStackStorage handler) {
		this(AllMountedStorageTypes.SIMPLE.get(), handler);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be == null)
			return;

		Storage<ItemVariant> cap = ItemStorage.SIDED.find(level, pos, state, be, null);
		if (cap != null) {
			validate(cap).ifPresent(handler -> {
				for (int i = 0; i < handler.getSlotCount(); i++) {
					handler.setStackInSlot(i, this.getStackInSlot(i));
				}
			});
		}
	}

	/**
	 * Make sure the targeted handler is valid for copying items back into.
	 * It is highly recommended to call super in overrides.
	 */
	protected Optional<SlottedStackStorage> validate(Storage<ItemVariant> handler) {
		if (!(handler instanceof SlottedStorage<ItemVariant> slottedStorage))
			return Optional.empty();

		if (slottedStorage.getSlotCount() == this.getSlotCount() && handler instanceof SlottedStackStorage modifiable) {
			return Optional.of(modifiable);
		} else {
			return Optional.empty();
		}
	}

	public static <T extends SimpleMountedStorage> MapCodec<T> codec(Function<SlottedStackStorage, T> factory) {
		return CreateCodecs.ITEM_STACK_HANDLER.xmap(factory, storage -> storage.wrapped).fieldOf("value");
	}
}
