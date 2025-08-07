package com.simibubi.create.api.equipment.goggles;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.foundation.utility.CreateLang;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.createmod.catnip.lang.LangBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Implement this interface on the {@link BlockEntity} that wants to add info to the goggle overlay
 */
public non-sealed interface IHaveGoggleInformation extends IHaveCustomOverlayIcon {
	/**
	 * This method will be called when looking at a {@link BlockEntity} that implements this interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be
	 * displayed, or {@code false} if the overlay should not be displayed
	 */
	default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	default boolean containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking,
										  Storage<FluidVariant> handler) {
		if (handler == null)
			return false;

		LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
		CreateLang.translate("gui.goggles.fluid_container")
			.forGoggles(tooltip);

		boolean isEmpty = true;
		int slotCount = 0;
		for (StorageView<FluidVariant> fluidStorage : handler) {
			slotCount++;
			if (fluidStorage.isResourceBlank() || fluidStorage.getAmount() <= 0)
				continue;

			CreateLang.fluidName(fluidStorage.getResource())
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip, 1);

			CreateLang.builder()
				.add(CreateLang.number(CreateTransferUtil.dropletsToMb(fluidStorage.getAmount()))
					.add(mb)
					.style(ChatFormatting.GOLD))
				.text(ChatFormatting.GRAY, " / ")
				.add(CreateLang.number(CreateTransferUtil.dropletsToMb(fluidStorage.getCapacity()))
					.add(mb)
					.style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip, 1);

			isEmpty = false;
		}

		if (slotCount == 0)
			return false;

		if (slotCount > 1) {
			if (isEmpty)
				tooltip.remove(tooltip.size() - 1);
			return true;
		}

		if (!isEmpty)
			return true;

		CreateLang.translate("gui.goggles.fluid_container.capacity")
			.add(CreateLang.number(CreateTransferUtil.dropletsToMb(handler.iterator().next().getCapacity()))
				.add(mb)
				.style(ChatFormatting.GOLD))
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip, 1);

		return true;
	}

}
