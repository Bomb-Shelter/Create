package com.simibubi.create.api.contraption.storage.item.chest;

import com.simibubi.create.infrastructure.fabric.transfer.CombinedInventoryStorage;
import com.simibubi.create.infrastructure.fabric.transfer.InventoryStorage;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

/**
 * Mounted storage that handles opening a combined GUI for double chests.
 */
public class ChestMountedStorage extends SimpleMountedStorage {
	public static final MapCodec<ChestMountedStorage> CODEC = SimpleMountedStorage.codec(ChestMountedStorage::new);

	protected ChestMountedStorage(MountedItemStorageType<?> type, SlottedStackStorage handler) {
		super(type, handler);
	}

	public ChestMountedStorage(ItemStackHandler handler) {
		this(AllMountedStorageTypes.CHEST.get(), handler);
	}

	public ChestMountedStorage(SlottedStorage<ItemVariant> storage) {
		this(copyToItemStackHandler(storage));
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		// the capability will include both sides of chests, but mounted storage is 1:1
		if (be instanceof Container container && this.getSlotCount() == container.getContainerSize()) {
			ItemHelper.copyContents(this, container);
		}
	}

	@Override
	protected SlottedStackStorage getHandlerForMenu(StructureBlockInfo info, Contraption contraption) {
		BlockState state = info.state();
		ChestType type = state.getValue(ChestBlock.TYPE);
		if (type == ChestType.SINGLE)
			return this;

		Direction facing = state.getValue(ChestBlock.FACING);
		Direction connectedDirection = ChestBlock.getConnectedDirection(state);
		BlockPos otherHalfPos = info.pos().relative(connectedDirection);

		MountedItemStorage otherHalf = this.getOtherHalf(contraption, otherHalfPos, state.getBlock(), facing, type);
		if (otherHalf == null)
			return this;

		if (type == ChestType.RIGHT) {
			return new CombinedInventoryStorage(this, otherHalf);
		} else {
			return new CombinedInventoryStorage(otherHalf, this);
		}
	}

	@Nullable
	protected MountedItemStorage getOtherHalf(Contraption contraption, BlockPos localPos, Block block,
											  Direction thisFacing, ChestType thisType) {
		StructureBlockInfo info = contraption.getBlocks().get(localPos);
		if (info == null)
			return null;
		BlockState state = info.state();
		if (!state.is(block))
			return null;

		Direction facing = state.getValue(ChestBlock.FACING);
		ChestType type = state.getValue(ChestBlock.TYPE);

		return facing == thisFacing && type == thisType.getOpposite()
			? contraption.getStorage().getMountedItems().storages.get(localPos)
			: null;
	}

	@Override
	protected void playOpeningSound(ServerLevel level, Vec3 pos) {
		level.playSound(
			null, BlockPos.containing(pos),
			SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
			0.75f, 1f
		);
	}

	@Override
	protected void playClosingSound(ServerLevel level, Vec3 pos) {
		level.playSound(
			null, BlockPos.containing(pos),
			SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
			0.75f, 1f
		);
	}
}
