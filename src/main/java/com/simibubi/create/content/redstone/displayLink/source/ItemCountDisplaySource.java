package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCountDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity sourceBE = context.getSourceBlockEntity();
		if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
			return ZERO.copy();

		InvManipulationBehaviour invManipulationBehaviour = cobe.getBehaviour(InvManipulationBehaviour.TYPE);
		FilteringBehaviour filteringBehaviour = cobe.getBehaviour(FilteringBehaviour.TYPE);
		Storage<ItemVariant> handler = invManipulationBehaviour.getInventory();

		if (handler == null)
			return ZERO.copy();

		int collected = 0;
		for (StorageView<ItemVariant> view : handler) {
			ItemStack stack = CreateTransferUtil.extractItem(view, view.getCapacity(), true);
			if (stack.isEmpty())
				continue;
			if (!filteringBehaviour.test(stack))
				continue;
			collected += stack.getCount();
		}

		return Component.literal(String.valueOf(collected));
	}

	@Override
	protected String getTranslationKey() {
		return "count_items";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
