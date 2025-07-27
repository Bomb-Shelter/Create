package com.simibubi.create.impl.unpacking;

import java.util.List;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public enum DefaultUnpackingHandler implements UnpackingHandler {
	INSTANCE;

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
		BlockEntity targetBE = level.getBlockEntity(pos);
		if (targetBE == null)
			return false;

		Storage<ItemVariant> targetInv = ItemStorage.SIDED.find(level, pos, state, targetBE, null);
		if (targetInv == null)
			return false;

		if (!simulate) {
			/*
			 * Some mods do not support slot-by-slot precision during simulate = false.
			 * Faulty interactions may lead to voiding of items, but the simulate pass should
			 * already have correctly identified there to be enough space for everything.
			 */
			for (ItemStack itemStack : items)
				CreateTransferUtil.insertItemStacked(targetInv, itemStack.copy(), false);
			return true;
		}

		for (StorageView<ItemVariant> view : targetInv) {
			ItemStack itemInSlot = CreateTransferUtil.getLimitedStack(view.getResource(), view.getAmount());
			int itemsAddedToSlot = 0;

			for (int boxSlot = 0; boxSlot < items.size(); boxSlot++) {
				ItemStack toInsert = items.get(boxSlot);
				if (toInsert.isEmpty())
					continue;

				if (CreateTransferUtil.insertItem(targetInv, toInsert, true) // Fabric TODO: this doesn't match behaviour, but we're missing insert on StorageView...
					.getCount() == toInsert.getCount())
					continue;

				if (itemInSlot.isEmpty()) {
					int maxStackSize = (int) view.getCapacity();
					if (maxStackSize < toInsert.getCount()) {
						toInsert.shrink(maxStackSize);
						toInsert = toInsert.copyWithCount(maxStackSize);
					} else
						items.set(boxSlot, ItemStack.EMPTY);

					itemInSlot = toInsert;
					CreateTransferUtil.insertItem(targetInv, toInsert, simulate);
					continue;
				}

				if (!ItemStack.isSameItemSameComponents(toInsert, itemInSlot))
					continue;

				int insertedAmount = toInsert.getCount() - CreateTransferUtil.insertItem(targetInv, toInsert, simulate)
					.getCount();
				int slotLimit = (int) ((view.isResourceBlank() || view.getAmount() <= 0
					 ? itemInSlot.getMaxStackSize() / 64f : 1) * view.getCapacity());
				int insertableAmountWithPreviousItems =
					Math.min(toInsert.getCount(), slotLimit - itemInSlot.getCount() - itemsAddedToSlot);

				int added = Math.min(insertedAmount, Math.max(0, insertableAmountWithPreviousItems));
				itemsAddedToSlot += added;

				items.set(boxSlot, toInsert.copyWithCount(toInsert.getCount() - added));
			}
		}

		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				// something failed to be inserted
				return false;
			}
		}

		return true;
	}
}
