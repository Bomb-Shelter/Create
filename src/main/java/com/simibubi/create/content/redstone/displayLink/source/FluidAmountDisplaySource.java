package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.utility.FluidFormatter;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class FluidAmountDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity sourceBE = context.getSourceBlockEntity();
		if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
			return EMPTY_LINE;

		TankManipulationBehaviour tankManipulationBehaviour = cobe.getBehaviour(TankManipulationBehaviour.OBSERVE);
		FilteringBehaviour filteringBehaviour = cobe.getBehaviour(FilteringBehaviour.TYPE);
		Storage<FluidVariant> handler = tankManipulationBehaviour.getInventory();

		if (handler == null)
			return EMPTY_LINE;

		long collected = 0;
		for (StorageView<FluidVariant> view : handler) {
			FluidStack stack = new FluidStack(view);
			if (stack.isEmpty())
				continue;
			if (!filteringBehaviour.test(stack))
				continue;
			collected += stack.getAmount();
		}

		return Component.literal(FluidFormatter.asString(collected, false));
	}

	@Override
	protected String getTranslationKey() {
		return "fluid_amount";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
