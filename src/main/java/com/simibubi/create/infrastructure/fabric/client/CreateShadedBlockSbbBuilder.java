package com.simibubi.create.infrastructure.fabric.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.render.ShadedBlockSbbBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import org.joml.Vector3f;

@SuppressWarnings("removal")
public class CreateShadedBlockSbbBuilder extends ShadedBlockSbbBuilder {
	private static final BakedQuad FAKE_QUAD = new BakedQuad(new int[0], -1, Direction.UP, Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation()), true);

	public static CreateShadedBlockSbbBuilder create() {
		return new CreateShadedBlockSbbBuilder();
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		prepareForGeometry(FAKE_QUAD);
		this.bufferBuilder.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		prepareForGeometry(FAKE_QUAD);
		this.bufferBuilder.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		prepareForGeometry(FAKE_QUAD);
		this.bufferBuilder.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		prepareForGeometry(FAKE_QUAD);
		this.bufferBuilder.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		prepareForGeometry(FAKE_QUAD);
		this.bufferBuilder.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		prepareForGeometry(FAKE_QUAD);
		this.bufferBuilder.setNormal(x, y, z);
		return this;
	}
}
