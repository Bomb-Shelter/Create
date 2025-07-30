package com.simibubi.create.foundation.item.render;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.world.item.Item;

public class SimpleCustomRenderer {
	public static DynamicItemRenderer create(Item item, CustomRenderedItemModelRenderer renderer) {
		CustomRenderedItems.register(item);
		return renderer;
	}
}
