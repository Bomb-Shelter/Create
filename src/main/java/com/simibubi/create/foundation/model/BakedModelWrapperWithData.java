package com.simibubi.create.foundation.model;

import com.simibubi.create.infrastructure.fabric.client.BakedModelWrapper;

import io.github.fabricators_of_create.porting_lib.models.data.ModelData;
import io.github.fabricators_of_create.porting_lib.models.data.ModelData.Builder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BakedModelWrapperWithData extends BakedModelWrapper<BakedModel> {

	public BakedModelWrapperWithData(BakedModel originalModel) {
		super(originalModel);
	}

	/*@Override
	public final ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData blockEntityData) {
		Builder builder = ModelData.builder();
		if (originalModel instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) originalModel).gatherModelData(builder, world, pos, state, blockEntityData);
		gatherModelData(builder, world, pos, state, blockEntityData);
		return builder.build();
	}*/

}
