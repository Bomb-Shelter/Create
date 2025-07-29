package com.simibubi.create.foundation.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.FluidRenderHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

@Environment(EnvType.CLIENT)
public class FluidRenderer {
	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, MultiBufferSource buffer, PoseStack ms, int light) {
		renderFluidStream(fluidStack, direction, radius, progress, inbound, FluidRenderHelper.getFluidBuilder(buffer), ms, light);
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
		Fluid fluid = fluidStack.getFluid();
		FluidVariantAttributeHandler fluidAttributes = FluidVariantAttributes.getHandlerOrDefault(fluid);
		TextureAtlasSprite[] textures = FluidVariantRendering.getSprites(fluidStack.getVariant());
		TextureAtlasSprite flowTexture = textures[1];
		TextureAtlasSprite stillTexture = textures[0];

		int color = FluidVariantRendering.getColor(fluidStack.getVariant());
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminance(fluidStack.getVariant()));
		light = (light & 0xF00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(direction))
			.rotateXDegrees(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)
			.uncenter();
		ms.translate(.5, 0, .5);

		float h = radius;
		float hMin = -radius;
		float hMax = radius;
		float y = inbound ? 1 : .5f;
		float yMin = y - Mth.clamp(progress * .5f, 0, 1);
		float yMax = y;

		for (int i = 0; i < 4; i++) {
			ms.pushPose();
			renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, hMax, yMax, h, builder, ms, light, color, flowTexture);
			ms.popPose();
			msr.rotateYDegrees(90);
		}

		if (progress != 1)
			FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, hMax, hMax, yMin, builder, ms, light, color, stillTexture);

		ms.popPose();
	}

	public static void renderFlowingTiledFace(Direction dir, float left, float down, float right, float up,
		float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 0.5f);
	}

}
