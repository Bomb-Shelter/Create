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

import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.Block;

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
 * {@link SlottedStorage} to {@link ItemStorage#SIDED}.
 * <br>
 * To use this implementation, either register {@link AllMountedStorageTypes#SIMPLE} to your block
 * manually, or add your block to the {@link AllTags.AllBlockTags#SIMPLE_MOUNTED_STORAGE} tag.
 * It is also possible to extend this class to create your own implementation.
 */
public class SimpleMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final MapCodec<SimpleMountedStorage> CODEC = codec(SimpleMountedStorage::new);

	public SimpleMountedStorage(MountedItemStorageType<?> type, SlottedStorage<ItemVariant> handler) {
		super(type, copyToItemStackHandler(handler));
	}

	public SimpleMountedStorage(SlottedStorage<ItemVariant> handler) {
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
					var slot = handler.getSlot(i);
					ItemStack toInsert = this.getStackInSlot(i);
					try (Transaction tx = TransferUtil.getTransaction()) {
						slot.extract(slot.getResource(), slot.getAmount(), tx);
						ItemVariant variant = ItemVariant.of(toInsert);

						long inserted = slot.insert(variant, toInsert.getCount(), tx);
						if (inserted != toInsert.getCount()) {
							long remainder = toInsert.getCount() - inserted;
							long fallbackInsert = handler.insert(variant, remainder, tx);
							if (remainder != fallbackInsert) {
								Block.popResource(level, pos, toInsert.copyWithCount((int) (fallbackInsert - remainder)));
							}
						}
						tx.commit();
					}
				}
			});
		}
	}

	/**
	 * Make sure the targeted handler is valid for copying items back into.
	 * It is highly recommended to call super in overrides.
	 */
	protected Optional<SlottedStorage<ItemVariant>> validate(Storage<ItemVariant> handler) {
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
