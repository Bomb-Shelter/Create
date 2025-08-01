package com.simibubi.create.infrastructure.fabric.client;

import io.github.fabricators_of_create.porting_lib.fluids.FluidType;

public abstract class CreateFluidType extends FluidType implements CustomRenderHandlerFluidType {
	/**
	 * Default constructor.
	 *
	 * @param properties the general properties of the fluid type
	 */
	public CreateFluidType(Properties properties) {
		super(properties);
	}
}
