package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import com.simibubi.create.foundation.item.ItemHelper;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class PackagePortAutomationInventoryWrapper extends ItemHandlerWrapper {

	private PackagePortBlockEntity ppbe;

	private boolean access;

	public PackagePortAutomationInventoryWrapper(SlottedStackStorage wrapped, PackagePortBlockEntity ppbe) {
		super(wrapped);
		this.ppbe = ppbe;
		access = false;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (access)
			return super.extract(resource, maxAmount, transaction);

		access = true;
		ItemStack extract = ItemHelper.extract(this, stack -> {
			if (!PackageItem.isPackage(stack))
				return false;
			String filterString = ppbe.getFilterString();
			return filterString != null && PackageItem.matchAddress(stack, filterString);
		}, true);

		(new FinalCommitSnapshot(maxAmount, () -> {
			ItemHelper.extract(this, stack -> {
				if (!PackageItem.isPackage(stack))
					return false;
				String filterString = ppbe.getFilterString();
				return filterString != null && PackageItem.matchAddress(stack, filterString);
			}, true);
		})).updateSnapshots(transaction);
		access = false;

		return extract.getCount();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		var stack = CreateTransferUtil.getLimitedStack(resource, maxAmount);
		if (!PackageItem.isPackage(stack))
			return 0;
		String filterString = ppbe.getFilterString();
		if (filterString != null && PackageItem.matchAddress(stack, filterString))
			return 0;

		return super.insert(resource, maxAmount, transaction);
	}
}
