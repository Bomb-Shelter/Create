package com.simibubi.create.content.contraptions.actors.psi;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

	protected Storage<ItemVariant> capability;

	public PortableItemInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	public static void registerCapabilities() {
		ItemStorage.SIDED.registerForBlockEntity(
			(be, context) -> be.capability,
			AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE.get()
		);
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		capability = new InterfaceItemHandler(contraption.getStorage().getAllItems());
		invalidateCapability();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		capability = createEmptyHandler();
		invalidateCapability();
		super.stopTransferring();
	}

	private SlottedStackStorage createEmptyHandler() {
		return new InterfaceItemHandler(new ItemStackHandler(0));
	}

	@Override
	protected void invalidateCapability() {
		//invalidateCapabilities();
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(SlottedStackStorage wrapped) {
			super(wrapped);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;

			long extracted = super.extract(resource, maxAmount, transaction);
			(new FinalCommitSnapshot(maxAmount, () -> {
				if (extracted > 0)
					onContentTransferred();
			})).updateSnapshots(transaction);

			return extracted;
		}

		@Override
		public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;

			long extracted = super.extractSlot(slot, resource, maxAmount, transaction);
			(new FinalCommitSnapshot(maxAmount, () -> {
				if (extracted > 0)
					onContentTransferred();
			})).updateSnapshots(transaction);

			return extracted;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;

			long inserted = super.insert(resource, maxAmount, transaction);

			(new FinalCommitSnapshot(maxAmount, () -> {
				if (inserted > 0)
					onContentTransferred();
			})).updateSnapshots(transaction);

			return inserted;
		}

		@Override
		public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;

			long inserted = super.insertSlot(slot, resource, maxAmount, transaction);

			(new FinalCommitSnapshot(maxAmount, () -> {
				if (inserted > 0)
					onContentTransferred();
			})).updateSnapshots(transaction);

			return inserted;
		}

	}

}
