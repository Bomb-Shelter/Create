package com.simibubi.create.compat.thresholdSwitch;

import com.simibubi.create.compat.Mods;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StorageDrawers implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity blockEntity) {
		return blockEntity != null && Mods.STORAGEDRAWERS.id()
			.equals(RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType())
				.getNamespace());
	}

	@Override
	public long getSpaceInSlot(SlottedStorage<ItemVariant> inv, int slot) {
		if (slot == 0)
			return 0;

		return inv.getSlot(slot).getCapacity();
	}
}
