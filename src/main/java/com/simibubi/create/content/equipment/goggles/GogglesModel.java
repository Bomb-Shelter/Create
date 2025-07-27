package com.simibubi.create.content.equipment.goggles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.infrastructure.fabric.client.BakedModelWrapper;

import io.github.fabricators_of_create.porting_lib.models.TransformTypeDependentItemBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;

public class GogglesModel extends BakedModelWrapper<BakedModel> implements TransformTypeDependentItemBakedModel {

	public GogglesModel(BakedModel template) {
		super(template);
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext cameraItemDisplayContext, PoseStack mat, boolean leftHanded, DefaultTransform defaultTransform) {
		if (cameraItemDisplayContext == ItemDisplayContext.HEAD)
			return TransformTypeDependentItemBakedModel
				.maybeApplyTransform(AllPartialModels.GOGGLES.get(), cameraItemDisplayContext, mat, leftHanded, defaultTransform);
		return TransformTypeDependentItemBakedModel.maybeApplyTransform(this.originalModel, cameraItemDisplayContext, mat, leftHanded, defaultTransform);
	}

}
