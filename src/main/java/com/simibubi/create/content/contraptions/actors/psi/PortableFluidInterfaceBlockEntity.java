package com.simibubi.create.content.contraptions.actors.psi;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.Contraption;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PortableFluidInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity implements SidedStorageBlockEntity {

	protected Storage<FluidVariant> capability;

	public PortableFluidInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	public static void registerCapabilities() {
	}

	@Override
	public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
		return capability;
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {;
		capability = new InterfaceFluidHandler(contraption.getStorage().getFluids());
		invalidateCapability();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void invalidateCapability() {
		//invalidateCapabilities();
	}

	@Override
	protected void stopTransferring() {
		capability = createEmptyHandler();
		invalidateCapability();
		super.stopTransferring();
	}

	private Storage<FluidVariant> createEmptyHandler() {
		return new InterfaceFluidHandler(new FluidTank(0));
	}

	public class InterfaceFluidHandler implements SlottedStorage<FluidVariant> {

		private SlottedStorage<FluidVariant> wrapped;

		public InterfaceFluidHandler(SlottedStorage<FluidVariant> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public int getSlotCount() {
			return wrapped.getSlotCount();
		}

		@Override
		public SingleSlotStorage<FluidVariant> getSlot(int slot) {
			return wrapped.getSlot(slot);
		}

		@Override
		public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			if (!isConnected())
				return 0;
			long fill = wrapped.insert(resource, maxAmount, transaction);
			(new FinalCommitSnapshot(maxAmount, () -> {
				if (fill > 0)
					keepAlive();
			})).updateSnapshots(transaction);
			return fill;
		}

		@Override
		public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;

			var drain = wrapped.extract(resource, maxAmount, transaction);
			(new FinalCommitSnapshot(maxAmount, () -> {
				if (drain > 0)
					keepAlive();
			})).updateSnapshots(transaction);
			return drain;
		}

		@Override
		public Iterator<StorageView<FluidVariant>> iterator() {
			return wrapped.iterator();
		}

		public void keepAlive() {
			onContentTransferred();
		}

	}

}
