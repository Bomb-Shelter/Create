package com.simibubi.create.foundation.mixin.accessor.ftb;

import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegionMapPanel.class)
public interface RegionMapPanelAccessor {
	@Accessor
	int getRegionMinX();

	@Accessor
	int getRegionMinZ();
}
