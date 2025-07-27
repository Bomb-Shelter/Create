package com.simibubi.create.infrastructure.fabric.crafting;

import com.mojang.serialization.MapCodec;

import com.simibubi.create.Create;

import io.github.fabricators_of_create.porting_lib.core.util.PortingLibExtraCodecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

/** Ingredient that matches if any of the child ingredients match */
public record CompoundIngredient(List<Ingredient> children) implements CustomIngredient {
	public static final CompoundIngredientSerializer SERIALIZER = new CompoundIngredientSerializer();

	public CompoundIngredient {
		if (children.isEmpty()) {
			// Empty ingredients are always represented as Ingredient.EMPTY.
			throw new IllegalArgumentException("Compound ingredient must have at least one child");
		}
	}

	/** Creates a compound ingredient from the given list of ingredients */
	public static Ingredient of(Ingredient... children) {
		if (children.length == 0)
			return Ingredient.EMPTY;
		if (children.length == 1)
			return children[0];

		return new CompoundIngredient(List.of(children)).toVanilla();
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return children.stream().flatMap(child -> Arrays.stream(child.getItems())).toList();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean test(ItemStack stack) {
		for (var child : children) {
			if (child.test(stack)) {
				return true;
			}
		}
		return false;
	}

	public static class CompoundIngredientSerializer implements CustomIngredientSerializer<CompoundIngredient> {
		public static final ResourceLocation ID = Create.asResource("compound");
		public static final MapCodec<CompoundIngredient> CODEC = PortingLibExtraCodecs.aliasedFieldOf(Ingredient.CODEC.listOf(), "children", "ingredients")
			.xmap(CompoundIngredient::new, CompoundIngredient::children);
		public static final StreamCodec<RegistryFriendlyByteBuf, CompoundIngredient> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.<RegistryFriendlyByteBuf, Ingredient>list().apply(Ingredient.CONTENTS_STREAM_CODEC), CompoundIngredient::children,
			CompoundIngredient::new
		);

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<CompoundIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CompoundIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
