package com.simibubi.create.infrastructure.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;

public interface CustomRenderHandlerFluidType {
	@Environment(EnvType.CLIENT)
	FluidRenderHandler getRenderHandler();
}
