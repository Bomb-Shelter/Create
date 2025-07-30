package com.simibubi.create.foundation.mixin.fabric;

import com.simibubi.create.CreateClient;

import net.minecraft.client.resources.model.AtlasSet.StitchResult;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;

import net.minecraft.client.resources.model.ModelManager.ReloadState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
	@Inject(method = "loadModels", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1))
	private void onLoadModels(ProfilerFiller profilerFiller, Map<ResourceLocation, StitchResult> atlasPreparations, ModelBakery modelBakery, CallbackInfoReturnable<ReloadState> cir) {
		// Its 100x easier to do this then use fabric's janky event
		CreateClient.MODEL_SWAPPER.onModelBake(modelBakery.getBakedTopLevelModels());
	}
}
