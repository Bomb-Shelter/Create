package com.simibubi.create.compat.jei;

import java.util.List;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements ISubtypeInterpreter<IJeiFluidIngredient> {
	@Override
	public @Nullable Object getSubtypeData(IJeiFluidIngredient ingredient, UidContext context) {
		if (ingredient.getFluidVariant().getComponents().isEmpty())
			return null;

		DataComponentMap components = ingredient.getFluidVariant().getComponentMap();
		PotionContents contents = components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		String potionTypeString = ingredient.getFluidVariant().getRegistryEntry().unwrapKey().orElseThrow().location().toLanguageKey("fluid");
		String bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR).name();

		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<MobEffectInstance> effects = contents.customEffects();

		stringBuilder.append(";")
			.append(bottleType);
		contents.potion().ifPresent(p -> {
			for (MobEffectInstance effect : p.value().getEffects())
				stringBuilder.append(";")
					.append(effect);
		});
		for (MobEffectInstance effect : effects)
			stringBuilder.append(";")
				.append(effect);
		return stringBuilder.toString();
	}

	@Override
	public String getLegacyStringSubtypeInfo(IJeiFluidIngredient ingredient, UidContext context) {
		return "";
	}
}
