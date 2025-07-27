package com.simibubi.create.compat.thresholdSwitch;

import com.simibubi.create.compat.Mods;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FunctionalStorage implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity blockEntity) {
		return blockEntity != null && Mods.FUNCTIONALSTORAGE.id()
			.equals(RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType())
				.getNamespace());
	}

	@Override
	public long getSpaceInSlot(SlottedStorage<ItemVariant> inv, int slot) {
		return inv.getSlot(slot).getCapacity();
	}
}
