package com.simibubi.create.infrastructure.data;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.compat.trinkets.TrinketsDataGenerator;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileTypes;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.CreateDatamapProvider;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.data.recipe.CreateSequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateStandardRecipeGen;
import com.simibubi.create.foundation.mixin.accessor.fabric.DataGenerator$PackGeneratorAccessor;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.infrastructure.worldgen.AllConfiguredFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacedFeatures;
import com.tterrag.registrate.providers.ProviderType;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

public class CreateDatagen implements DataGeneratorEntrypoint {
	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.ENCHANTMENT, AllEnchantments::bootstrap)
			.add(Registries.DAMAGE_TYPE, AllDamageTypes::bootstrap)
			.add(Registries.CONFIGURED_FEATURE, AllConfiguredFeatures::bootstrap)
			.add(Registries.PLACED_FEATURE, AllPlacedFeatures::bootstrap)
			//.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AllBiomeModifiers::bootstrap)
			.add(CreateRegistries.POTATO_PROJECTILE_TYPE, AllPotatoProjectileTypes::bootstrap);
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		Pack generator = fabricDataGenerator.createPack();
		//CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper existingFileHelper = ExistingFileHelper.withResourcesFromArg();

		Create.registrate().onData(generator, existingFileHelper);

		if (fabricDataGenerator.getModId().equals(Create.ID))
			addExtraRegistrateData();

		generator.addProvider((FabricDataOutput output) -> AllSoundEvents.provider(fabricDataGenerator, output));

		generator.addProvider((output, lookupProvider) -> new CreateRecipeSerializerTagsProvider(output, lookupProvider, existingFileHelper));
		generator.addProvider((output, lookupProvider) -> new CreateContraptionTypeTagsProvider(output, lookupProvider, existingFileHelper));
		generator.addProvider((output, lookupProvider) -> new CreateMountedItemStorageTypeTagsProvider(output, lookupProvider, existingFileHelper));
		generator.addProvider((output, lookupProvider) -> new DamageTypeTagGen(output, lookupProvider, existingFileHelper));
		generator.addProvider((output, lookupProvider) -> new AllAdvancements(output, lookupProvider));
		generator.addProvider((output, lookupProvider) -> new CreateStandardRecipeGen(output, lookupProvider));
		generator.addProvider((output, lookupProvider) -> new CreateMechanicalCraftingRecipeGen(output, lookupProvider));
		generator.addProvider((output, lookupProvider) -> new CreateSequencedAssemblyRecipeGen(output, lookupProvider));
		generator.addProvider((output, lookupProvider) -> new CreateDatamapProvider(output, lookupProvider));
		generator.addProvider((output, lookupProvider) -> new VanillaHatOffsetGenerator(output, lookupProvider));
		//generator.addProvider(new TrinketsDataGenerator(output, lookupProvider, existingFileHelper));
		generator.addProvider((output, lookupProvider) -> new CreateEnchantmentTagsProvider(output, lookupProvider, existingFileHelper));
		generator.addProvider((output, lookupProvider) -> new GeneratedEntriesProvider(output, lookupProvider));

		CreateRecipeProvider.registerAllProcessing(generator);
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		Create.registrate().addDataGenerator(ProviderType.LANG, provider -> {
			BiConsumer<String, String> langConsumer = provider::add;

			provideDefaultLang("interface", langConsumer);
			provideDefaultLang("tooltips", langConsumer);
			AllAdvancements.provideLang(langConsumer);
			AllSoundEvents.provideLang(langConsumer);
			AllKeys.provideLang(langConsumer);
			providePonderLang(langConsumer);
		});
	}

	private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
		String path = "assets/create/lang/default/" + fileName + ".json";
		JsonElement jsonElement = FilesHelper.loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			consumer.accept(key, value);
		}
	}

	private static void providePonderLang(BiConsumer<String, String> consumer) {
		// Register this since FMLClientSetupEvent does not run during datagen
		PonderIndex.addPlugin(new CreatePonderPlugin());

		PonderIndex.getLangAccess().provideLang(Create.ID, consumer);
	}
}
