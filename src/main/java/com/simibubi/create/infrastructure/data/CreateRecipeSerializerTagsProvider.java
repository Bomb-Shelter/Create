package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.fabricators_of_create.porting_lib.data.PortingLibTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags.AllRecipeSerializerTags;
import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CreateRecipeSerializerTagsProvider extends PortingLibTagsProvider<RecipeSerializer<?>> {
	public CreateRecipeSerializerTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, Registries.RECIPE_SERIALIZER, lookupProvider, Create.ID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(Mods.OCCULTISM.rl("spirit_trade"))
		.addOptional(Mods.OCCULTISM.rl("ritual"));

		// VALIDATE

		for (AllRecipeSerializerTags tag : AllRecipeSerializerTags.values()) {
			if (tag.alwaysDatagen) {
				getOrCreateRawBuilder(tag.tag);
			}
		}

	}

	@Override
	public String getName() {
		return "Create's Recipe Serializer Tags";
	}
}
