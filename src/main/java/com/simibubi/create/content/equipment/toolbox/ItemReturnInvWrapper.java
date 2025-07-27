package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.infrastructure.fabric.transfer.ExtractOnlySingleSlotStorage;
import com.simibubi.create.infrastructure.fabric.transfer.PlayerInventoryStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * For inserting items into a players' inventory anywhere except the hotbar
 */
public class ItemReturnInvWrapper extends PlayerInventoryStorage {

	public ItemReturnInvWrapper(Inventory inv) {
		super(inv);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return new ExtractOnlySingleSlotStorage<>(super.getSlot(slot));
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (slot >= 0 && slot < 9)
			return 0;

		return super.insertSlot(slot, resource, maxAmount, transaction);
	}

}
