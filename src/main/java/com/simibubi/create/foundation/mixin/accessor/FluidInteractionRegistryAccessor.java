package com.simibubi.create.foundation.mixin.accessor;

import java.util.List;
import java.util.Map;

import io.github.fabricators_of_create.porting_lib.fluids.FluidInteractionRegistry;

import io.github.fabricators_of_create.porting_lib.fluids.FluidInteractionRegistry.InteractionInformation;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidInteractionRegistry.class)
public interface FluidInteractionRegistryAccessor {
	@Accessor(value = "INTERACTIONS", remap = false)
	static Map<FluidType, List<InteractionInformation>> getInteractions() {
		throw new AssertionError();
	}
}
