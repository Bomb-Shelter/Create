package com.simibubi.create.infrastructure.fabric.client;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.Nullable;

public abstract class CreateFluidRenderHandler implements FluidRenderHandler {
	public abstract ResourceLocation getStillTexture();
	public abstract ResourceLocation getFlowingTexture();

	private TextureAtlasSprite stillTexture;
	private TextureAtlasSprite flowingTexture;

	@Override
	public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
		return new TextureAtlasSprite[] {
			stillTexture,
			flowingTexture
		};
	}

	public int getTintColor(FluidState state, BlockAndTintGetter level, BlockPos pos) {
		return FluidRenderHandler.super.getFluidColor(level, pos, state);
	}

	@Override
	public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
		return getTintColor(state, view, pos);
	}

	@Override
	public void reloadTextures(TextureAtlas textureAtlas) {
		this.stillTexture = textureAtlas.getSprite(this.getStillTexture());
		this.flowingTexture = textureAtlas.getSprite(this.getFlowingTexture());
	}
}
