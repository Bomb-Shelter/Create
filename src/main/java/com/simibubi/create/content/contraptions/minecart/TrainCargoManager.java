package com.simibubi.create.content.contraptions.minecart;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorageManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class TrainCargoManager extends MountedStorageManager {

	int ticksSinceLastExchange;
	AtomicInteger version;

	public TrainCargoManager() {
		version = new AtomicInteger();
		ticksSinceLastExchange = 0;
	}

	@Override
	public void initialize() {
		super.initialize();
		this.items = new CargoInvWrapper(this.items);
		this.allItems = this.items;
		if (this.fuelItems != null) {
			this.fuelItems = new CargoInvWrapper(this.fuelItems);
		}
		this.fluids = new CargoTankWrapper(this.fluids);
	}

	@Override
	public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(nbt, registries, clientPacket);
		nbt.putInt("TicksSinceLastExchange", ticksSinceLastExchange);
	}

	@Override
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, @Nullable Contraption contraption) {
		super.read(nbt, registries, clientPacket, contraption);
		ticksSinceLastExchange = nbt.getInt("TicksSinceLastExchange");
	}

	public void resetIdleCargoTracker() {
		ticksSinceLastExchange = 0;
	}

	public void tickIdleCargoTracker() {
		ticksSinceLastExchange++;
	}

	public int getTicksSinceLastExchange() {
		return ticksSinceLastExchange;
	}

	public int getVersion() {
		return version.get();
	}

	void changeDetected() {
		version.incrementAndGet();
		resetIdleCargoTracker();
	}

	class CargoInvWrapper extends MountedItemStorageWrapper {
		CargoInvWrapper(MountedItemStorageWrapper wrapped) {
			super(wrapped.storages);
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			long inserted = super.insert(resource, maxAmount, transaction);
			if (inserted > 0)
				new FinalCommitSnapshot(maxAmount, TrainCargoManager.this::changeDetected)
					.updateSnapshots(transaction);

			return inserted;
		}

		@Override
		public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
			long inserted = super.insertSlot(slot, resource, maxAmount, transaction);
			if (inserted > 0)
				new FinalCommitSnapshot(maxAmount, TrainCargoManager.this::changeDetected)
					.updateSnapshots(transaction);

			return inserted;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			long extracted = super.extract(resource, maxAmount, transaction);
			if (extracted > 0)
				new FinalCommitSnapshot(maxAmount, TrainCargoManager.this::changeDetected)
					.updateSnapshots(transaction);

			return extracted;
		}

		@Override
		public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
			long extracted = super.extractSlot(slot, resource, maxAmount, transaction);
			if (extracted > 0)
				new FinalCommitSnapshot(maxAmount, TrainCargoManager.this::changeDetected)
					.updateSnapshots(transaction);

			return extracted;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (!stack.equals(getStackInSlot(slot)))
				changeDetected();
			super.setStackInSlot(slot, stack);
		}

	}

	class CargoTankWrapper extends MountedFluidStorageWrapper {
		CargoTankWrapper(MountedFluidStorageWrapper wrapped) {
			super(wrapped.storages);
		}

		@Override
		public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			long filled = super.insert(resource, maxAmount, transaction);
			if (filled > 0)
				new FinalCommitSnapshot(maxAmount, TrainCargoManager.this::changeDetected)
					.updateSnapshots(transaction);
			return filled;
		}

		@Override
		public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			long drained = super.extract(resource, maxAmount, transaction);
			if (drained > 0)
				new FinalCommitSnapshot(maxAmount, TrainCargoManager.this::changeDetected)
					.updateSnapshots(transaction);

			return drained;
		}

	}

}
