package com.simibubi.create.infrastructure.fabric.transfer;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CreateTransferUtil {

	public static final long BOTTLE = FluidConstants.BLOCK / 4;

	public static <T> long simulateInsert(Storage<T> storage, T variant, long amount) {
		if (!storage.supportsInsertion())
			return 0;

		try (Transaction t = TransferUtil.getTransaction()) {
			return storage.insert(variant, amount, t);
		}
	}

	public static <T> long simulateExtract(Storage<T> storage, T variant, long amount) {
		if (!storage.supportsExtraction())
			return 0;

		try (Transaction t = TransferUtil.getTransaction()) {
			return storage.extract(variant, amount, t);
		}
	}

	public static long simulateInsertFluid(Storage<FluidVariant> storage, FluidStack stack) {
		return simulateInsert(storage, stack.getVariant(), stack.getAmount());
	}

	public static long simulateExtractFluid(Storage<FluidVariant> storage, FluidStack stack) {
		return simulateExtract(storage, stack.getVariant(), stack.getAmount());
	}

	public static long simulateInsertItem(Storage<ItemVariant> storage, ItemStack stack) {
		if (!storage.supportsInsertion())
			return 0;

		try (Transaction t = TransferUtil.getTransaction()) {
			return storage.insert(ItemVariant.of(stack), stack.getCount(), t);
		}
	}

	public static long simulateExtractAnyItem(StorageView<ItemVariant> storage, long maxAmount) {
		return simulateExtractAny(storage, maxAmount);
	}

	public static long extractAnyItem(StorageView<ItemVariant> storage, long maxAmount) {
		// This is technically a porting lib bug, but I'm to lazy to patch it so we are just going to do a workaround
		if (storage.isResourceBlank())
			return 0;
		try (Transaction t = TransferUtil.getTransaction()) {
			var result = storage.extract(storage.getResource(), maxAmount, t);
			t.commit();
			return result;
		}
	}

	public static <T> long simulateExtractAny(StorageView<T> storage, long maxAmount) {
		try (Transaction t = TransferUtil.getTransaction()) {
			return storage.extract(storage.getResource(), maxAmount, t);
		}
	}

	public static SlottedStorage<FluidVariant> getSlottedFluidStorage(Level level, BlockPos pos, BlockEntity be, @Nullable Direction side) {
		var storage = TransferUtil.getFluidStorage(level, pos, be, side);

		if (!(storage instanceof SlottedStorage<FluidVariant> slotted)) {
			return null;
		}

		return slotted;
	}

	public static FluidStack getFluidStack(SingleSlotStorage<FluidVariant> slot) {
		return new FluidStack(slot);
	}

	public static ItemStack getLimitedStack(ItemVariant variant, long maxAmount) {
		return variant.toStack((int) Math.min(maxAmount, getMaxStackSize(variant)));
	}

	public static int getMaxStackSize(ItemVariant variant) {
		return variant.getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, variant.getItem().getDefaultMaxStackSize());
	}

	public static int getMaxStackSize(ItemStack stack) {
		return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, stack.getItem().getDefaultMaxStackSize());
	}

	// A bridge between Forge's transfer API and Fabric's, basically
	public static ItemStack insertItem(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);

			if (!simulate)
				transaction.commit();

			if (inserted >= stack.getCount())
				return ItemStack.EMPTY;

			return stack.copyWithCount(stack.getCount() - (int) inserted);
		}
	}

	public static ItemStack insertItemStacked(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
		ItemStack remainder = stack.copy();
		long inserted = simulate ? CreateTransferUtil.simulateInsertItem(storage, stack) : TransferUtil.insertItem(storage, stack);

		remainder.shrink((int) inserted);

		return remainder;
	}

	public static ItemStack extractItem(Storage<ItemVariant> storage, int slot, ItemStack stack, boolean simulate) {
		int current = 0;
		for (StorageView<ItemVariant> view : storage) {
			if (current++ == slot) {
				return extractItem(view, stack, simulate);
			}
		}

		return ItemStack.EMPTY;
	}

	public static ItemStack extractItem(Storage<ItemVariant> storage, int slot, long amount, boolean simulate) {
		if (storage instanceof SlottedStorage<ItemVariant> slotted) {
			return extractItem(slotted.getSlot(slot), amount, simulate);
		}
		int current = 0;
		for (StorageView<ItemVariant> view : storage) {
			if (current++ == slot) {
				return extractItem(view, amount, simulate);
			}
		}

		return ItemStack.EMPTY;
	}

	public static ItemStack extractItem(StorageView<ItemVariant> storage, ItemStack stack, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			var inserted = storage.extract(ItemVariant.of(stack), stack.getCount(), transaction);

			if (!simulate)
				transaction.commit();

			return stack.copyWithCount((int) Math.min(stack.getCount() - inserted, 0));
		}
	}

	public static ItemStack extractItem(StorageView<ItemVariant> storage, long amount, boolean simulate) {
		if (storage.isResourceBlank())
			return ItemStack.EMPTY;
		try (Transaction transaction = TransferUtil.getTransaction()) {
			var resource = storage.getResource();
			int maxSize = resource.getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, resource.getItem().getDefaultMaxStackSize());
			if (amount > maxSize) {
				amount = maxSize;
			}
			var extracted = storage.extract(resource, amount, transaction);

			if (!simulate)
				transaction.commit();

			return resource.toStack((int) extracted);
		}
	}

	public static FluidStack extractAnyFluid(Storage<FluidVariant> storage, long amount, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			for (StorageView<FluidVariant> view : storage) {
				if (!view.isResourceBlank() && view.getAmount() > 0) {
					long extracted = storage.extract(view.getResource(), amount, transaction);
					var stack = new FluidStack(view.getResource(), extracted);

					if (!simulate)
						transaction.commit();

					return stack;
				}
			}
		}

		return FluidStack.EMPTY;
	}

	/**
	 * Extract anything matching the given predicate, or null if none available.
	 * based on {@link StorageUtil#extractAny(Storage, long, TransactionContext)}
	 */
	@Nullable
	public static FluidStack extractFluidMatching(Storage<FluidVariant> storage, Predicate<FluidStack> predicate,
																				   long maxAmount, boolean simulate) {
		StoragePreconditions.notNegative(maxAmount);

		if (storage == null) return null;
		try (Transaction transaction = TransferUtil.getTransaction()) {
			try {
				for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
					long amount = view.extract(view.getResource(), maxAmount, transaction);
					FluidStack resource = new FluidStack(view.getResource(), amount);
					if (predicate.test(resource)) { // only addition
						if (amount > 0) return resource.copyWithAmount(amount);
					}
				}
				if (!simulate)
					transaction.commit();
			} catch (Exception e) {
				CrashReport report = CrashReport.forThrowable(e, "Extracting resources from storage");
				report.addCategory("Extraction details")
					.setDetail("Storage", storage::toString)
					.setDetail("Max amount", maxAmount)
					.setDetail("Transaction", transaction);
				throw new ReportedException(report);
			}
		}

		return FluidStack.EMPTY;
	}

	public static long insertFluid(Storage<FluidVariant> storage, FluidStack stack, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			var inserted = storage.insert(stack.getVariant(), stack.getAmount(), transaction);

			if (!simulate)
				transaction.commit();

			return inserted;
		}
	}

	public static FluidStack extractFluid(Storage<FluidVariant> storage, FluidStack stack, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			var extracted = storage.extract(stack.getVariant(), stack.getAmount(), transaction);

			if (!simulate)
				transaction.commit();

			return new FluidStack(stack.getVariant(), extracted);
		}
	}

	public static FluidStack extractFluid(Storage<FluidVariant> storage, long amount, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			for (StorageView<FluidVariant> view : storage) {
				var variant = view.getResource();
				if (variant.isBlank())
					continue;

				var extracted = storage.extract(variant, amount, transaction);

				if (!simulate)
					transaction.commit();

				return new FluidStack(variant, extracted);
			}
		}

		return FluidStack.EMPTY;
	}

	public static FluidStack extractFluid(StorageView<FluidVariant> storage, FluidVariant variant, long amount, boolean simulate) {
		try (Transaction transaction = TransferUtil.getTransaction()) {
			var extracted = storage.extract(variant, amount, transaction);

			if (!simulate)
				transaction.commit();

			return new FluidStack(variant, extracted);
		}
	}

	public static <T> int getSlotCount(Storage<T> storage) {
		int count = 0;

		for (StorageView<T> view : storage) {
			count++;
		}

		return count;
	}

	public static long mbToDroplets(int millibuckets) {
		return (long) ((millibuckets / 1000.0) * (double) FluidConstants.BLOCK);
	}

	public static int dropletsToMb(long droplets) {
		return (int) ((droplets / (double) FluidConstants.BLOCK) * 1000.0);
	}
}
