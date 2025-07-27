package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import io.github.fabricators_of_create.porting_lib.models.data.ModelData;
import io.github.fabricators_of_create.porting_lib.models.data.ModelProperty;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedKineticBlockModel extends ForwardingBakedModel {

	private static final ModelProperty<BracketedModelData> BRACKET_PROPERTY = new ModelProperty<>();

	public BracketedKineticBlockModel(BakedModel template) {
		this.wrapped = template;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter world, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (world instanceof EmptyVirtualBlockGetter) {
			super.emitBlockQuads(world, state, pos, randomSupplier, context);
			return;
		}

		BracketedModelData data = new BracketedModelData();
		BracketedBlockEntityBehaviour attachmentBehaviour =
			BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());

		BakedModel bracket = data.getBracket();
		if (bracket != null)
			bracket.emitBlockQuads(world, state, pos, randomSupplier, context);
	}

	private static class BracketedModelData {
		private BakedModel bracket;

		public void putBracket(BlockState state) {
			if (state != null) {
				this.bracket = Minecraft.getInstance()
					.getBlockRenderer()
					.getBlockModel(state);
			}
		}

		public BakedModel getBracket() {
			return bracket;
		}
	}

}
