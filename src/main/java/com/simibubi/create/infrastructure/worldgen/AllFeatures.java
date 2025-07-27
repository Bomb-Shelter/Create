package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import io.github.fabricators_of_create.porting_lib.registry.DeferredHolder;
import io.github.fabricators_of_create.porting_lib.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllFeatures {
	private static final DeferredRegister<Feature<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.FEATURE, Create.ID);

	public static final DeferredHolder<Feature<?>, LayeredOreFeature> LAYERED_ORE = REGISTER.register("layered_ore", () -> new LayeredOreFeature());

	@Internal
	public static void register() {
		REGISTER.register();
	}
}
