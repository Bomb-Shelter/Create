package com.simibubi.create.foundation.mixin.accessor.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
	@Invoker
	void invokeRenderQuadList(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay);
}
