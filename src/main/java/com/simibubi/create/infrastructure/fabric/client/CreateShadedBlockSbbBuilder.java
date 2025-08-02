package com.simibubi.create.infrastructure.fabric.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.render.ShadedBlockSbbBuilder;

import net.createmod.ponder.mixin.client.accessor.BufferBuilderAccessor;

@SuppressWarnings("removal")
public class CreateShadedBlockSbbBuilder extends ShadedBlockSbbBuilder {
	public static CreateShadedBlockSbbBuilder create() {
		return new CreateShadedBlockSbbBuilder();
	}

	private void prepareForGeometry(boolean shade) {
		if (shade != currentShade) {
			shadeSwapVertices.add(((BufferBuilderAccessor) bufferBuilder).catnip$getVertices());
			currentShade = shade;
		}
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		prepareForGeometry(true);
		this.bufferBuilder.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		prepareForGeometry(true);
		this.bufferBuilder.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		prepareForGeometry(true);
		this.bufferBuilder.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		prepareForGeometry(true);
		this.bufferBuilder.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		prepareForGeometry(true);
		this.bufferBuilder.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		prepareForGeometry(true);
		this.bufferBuilder.setNormal(x, y, z);
		return this;
	}
}
