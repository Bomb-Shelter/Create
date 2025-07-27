package com.simibubi.create.foundation.recipe;

import com.simibubi.create.Create;

import com.simibubi.create.foundation.data.SimpleDatagenIngredient;

import com.simibubi.create.foundation.data.SimpleDatagenIngredient.Serializer;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllIngredients {
	//public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Create.ID);

	// Unused currently

	@Internal
	public static void register() {
		CustomIngredientSerializer.register(Serializer.SERIALIZER);
		//INGREDIENT_TYPES.register(modEventBus);
	}
}
