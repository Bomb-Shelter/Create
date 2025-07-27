package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SimpleCustomRenderer implements DynamicItemRenderer {

	protected CustomRenderedItemModelRenderer renderer;

	protected SimpleCustomRenderer(CustomRenderedItemModelRenderer renderer) {
		this.renderer = renderer;
	}

	public static SimpleCustomRenderer create(Item item, CustomRenderedItemModelRenderer renderer) {
		CustomRenderedItems.register(item);
		return new SimpleCustomRenderer(renderer);
	}

	@Override
	public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		this.renderer.render(stack, mode, matrices, vertexConsumers, light, overlay);
	}
}
