package com.simibubi.create.compat.thresholdSwitch;

import com.simibubi.create.compat.Mods;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SophisticatedStorage implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity be) {
		if (be == null)
			return false;

		String namespace = RegisteredObjectsHelper.getKeyOrThrow(be.getType())
			.getNamespace();

		return
			Mods.SOPHISTICATEDSTORAGE.id().equals(namespace)
			|| Mods.SOPHISTICATEDBACKPACKS.id().equals(namespace);
	}

	@Override
	public long getSpaceInSlot(SlottedStorage<ItemVariant> inv, int slot) {
		return ((long) inv.getSlot(slot).getCapacity() * inv.getSlot(slot).getResource().getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, 64)) / 64;
	}

}
