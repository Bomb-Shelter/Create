package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

public class CreateEnchantmentTagsProvider extends EnchantmentTagsProvider {
	public CreateEnchantmentTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider/*, Create.ID, existingFileHelper*/);
	}

	@Override
	protected void addTags(Provider prov) {
		tag(EnchantmentTags.NON_TREASURE)
			.add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
		tag(EnchantmentTags.IN_ENCHANTING_TABLE)
			.add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
	}
}
