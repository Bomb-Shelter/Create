package com.simibubi.create.content.contraptions.actors.psi;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import com.simibubi.create.infrastructure.fabric.transfer.view.ListeningStorageView;
import com.simibubi.create.infrastructure.fabric.transfer.view.ProcessingIterator;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

	protected InterfaceItemHandler capability;

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
		capability.setWrapped(contraption.getStorage().getAllItems());
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		invalidateCapability();
		super.stopTransferring();
	}

	private InterfaceItemHandler createEmptyHandler() {
		return new InterfaceItemHandler(Storage.empty());
	}

	@Override
	protected void invalidateCapability() {
		capability.setWrapped(Storage.empty());
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(Storage<ItemVariant> wrapped) {
			super(wrapped);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long extracted = super.extract(resource, maxAmount, transaction);
			if (extracted != 0) {
				TransactionCallback.onSuccess(transaction, PortableItemInterfaceBlockEntity.this::onContentTransferred);
			}
			return extracted;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long inserted = super.insert(resource, maxAmount, transaction);
			if (inserted != 0) {
				TransactionCallback.onSuccess(transaction, PortableItemInterfaceBlockEntity.this::onContentTransferred);
			}
			return inserted;
		}

		@Override
		public Iterator<StorageView<ItemVariant>> iterator() {
			return new ProcessingIterator<>(super.iterator(), this::listen);
		}

		public <T> StorageView<T> listen(StorageView<T> view) {
			return new ListeningStorageView<>(view, PortableItemInterfaceBlockEntity.this::onContentTransferred);
		}

		private void setWrapped(Storage<ItemVariant> wrapped) {
			this.wrapped = wrapped;
		}
	}

}
