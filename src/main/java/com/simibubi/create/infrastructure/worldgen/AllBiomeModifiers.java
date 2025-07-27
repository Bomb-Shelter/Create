package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class AllBiomeModifiers {
	public static void bootstrap() {
		BiomeModifications.create(Create.asResource("ores"))
			.add(ModificationPhase.ADDITIONS, context -> context.hasTag(BiomeTags.IS_OVERWORLD), (selectionContext, modificationContext) -> {
				var generationSettings = modificationContext.getGenerationSettings();
				generationSettings.addFeature(Decoration.UNDERGROUND_ORES, AllPlacedFeatures.ZINC_ORE);
				generationSettings.addFeature(Decoration.UNDERGROUND_ORES, AllPlacedFeatures.STRIATED_ORES_OVERWORLD);
			})
			.add(ModificationPhase.ADDITIONS, context -> context.hasTag(BiomeTags.IS_NETHER), (selectionContext, modificationContext) -> {
				var generationSettings = modificationContext.getGenerationSettings();
				generationSettings.addFeature(Decoration.UNDERGROUND_ORES, AllPlacedFeatures.STRIATED_ORES_NETHER);
			});
	}

}
