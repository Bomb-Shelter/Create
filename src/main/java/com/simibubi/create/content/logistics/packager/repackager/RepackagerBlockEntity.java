package com.simibubi.create.content.logistics.packager.repackager;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.packager.PackagingRequest;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class RepackagerBlockEntity extends PackagerBlockEntity implements SidedStorageBlockEntity {

	public PackageRepackageHelper repackageHelper;

	public RepackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		repackageHelper = new PackageRepackageHelper();
	}

	public boolean unwrapBox(ItemStack box, boolean simulate) {
		if (animationTicks > 0)
			return false;

		Storage<ItemVariant> targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return false;

		boolean targetIsCreativeCrate = targetInv instanceof BottomlessItemHandler;
		boolean anySpace = false;

		try (Transaction transaction = TransferUtil.getTransaction()) {
			for (StorageView<ItemVariant> view : targetInv) {
				long inserted = targetInv.insert(ItemVariant.of(box), box.getCount(), transaction);
				if (inserted > 0)
					continue;

				if (!simulate) {
					box.shrink((int) inserted);
				}

				anySpace = true;
				break;
			}

			if (!simulate)
				transaction.commit();
		}

		if (!targetIsCreativeCrate && !anySpace)
			return false;
		if (simulate)
			return true;

		previouslyUnwrapped = box;
		animationInward = true;
		animationTicks = CYCLE;
		notifyUpdate();
		return true;
	}

	@Override
	public void recheckIfLinksPresent() {
	}

	@Override
	public boolean redstoneModeActive() {
		return true;
	}

	public void attemptToSend(List<PackagingRequest> queuedRequests) {
		if (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0)
			return;
		if (!queuedExitingPackages.isEmpty())
			return;

		Storage<ItemVariant> targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return;

		attemptToRepackage(targetInv);
		if (heldBox.isEmpty())
			return;

		updateSignAddress();
		if (!signBasedAddress.isBlank())
			PackageItem.addAddress(heldBox, signBasedAddress);
	}

	protected void attemptToRepackage(Storage<ItemVariant> targetInv) {
		repackageHelper.clear();
		int completedOrderId = -1;

		for (StorageView<ItemVariant> view : targetInv) {
			ItemStack extracted = view.getResource().toStack(1);
			long extractedTotal = CreateTransferUtil.simulateExtractAnyItem(view, 1);

			if (extractedTotal <= 0 || !PackageItem.isPackage(extracted))
				continue;

			if (!repackageHelper.isFragmented(extracted)) {
				CreateTransferUtil.extractAnyItem(view, 1);
				heldBox = extracted.copy();
				animationInward = false;
				animationTicks = CYCLE;
				notifyUpdate();
				return;
			}

			completedOrderId = repackageHelper.addPackageFragment(extracted);
			if (completedOrderId != -1)
				break;
		}

		if (completedOrderId == -1)
			return;

		List<BigItemStack> boxesToExport = repackageHelper.repack(completedOrderId, level.getRandom());

		for (StorageView<ItemVariant> view : targetInv) {
			long extractedTotal = CreateTransferUtil.simulateExtractAnyItem(view, 1);
			ItemStack extracted = view.getResource().toStack(1);
			if (extractedTotal <= 0 || !PackageItem.isPackage(extracted))
				continue;
			if (PackageItem.getOrderId(extracted) != completedOrderId)
				continue;
			CreateTransferUtil.extractAnyItem(view, 1);
		}

		if (boxesToExport.isEmpty())
			return;

		queuedExitingPackages.addAll(boxesToExport);
		notifyUpdate();
	}

	public static void registerCapabilities() {
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
		return this.inventory;
	}
}
