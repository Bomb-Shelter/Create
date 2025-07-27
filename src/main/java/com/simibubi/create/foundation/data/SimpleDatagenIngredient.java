package com.simibubi.create.foundation.data;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.simibubi.create.Create;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.mixin.accessor.MappedRegistryAccessor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SimpleDatagenIngredient implements CustomIngredient {

	/*
	"ingredients": [
		{
			"item": "mod:compat_item"
		}
	]
	 */

	private static final MapCodec<SimpleDatagenIngredient> INTERNAL_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("item").forGetter(i -> i.mod.asResource(i.id))
	).apply(instance, location -> {
		for (Mods mod : Mods.values()) {
			if (mod.getId().equals(location.getNamespace())) {
				return new SimpleDatagenIngredient(mod, location.getPath());
			}
		}
		throw new AssertionError("ID "+location.getNamespace()+" doesn't correspond to any compat mod.");
	}));

	private static final MapCodec<SimpleDatagenIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			INTERNAL_CODEC.codec().listOf().fieldOf("ingredients").forGetter(List::of)
	).apply(instance, list -> {
		assert list.size() == 1 : "SimpleDatagenIngredient should only be serialized as a single-element list, and shouldn't be deserialized anyway";
		return list.getFirst();
	}));
	//private static final IngredientType<?> INGREDIENT_TYPE = new IngredientType<>(CODEC);

	private final Mods mod;
	private final String id;

	public SimpleDatagenIngredient(Mods mod, String id) {
		this.mod = mod;
		this.id = id;
	}

	@Override
	public boolean test(@NotNull ItemStack stack) {
		return stack.getItemHolder().unwrapKey().orElseThrow().location().equals(mod.asResource(id));
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		var id = ResourceLocation.fromNamespaceAndPath(mod.getId(), this.id);
		if (BuiltInRegistries.ITEM.getOptional(id).isPresent())
			return List.of(new ItemStack(BuiltInRegistries.ITEM.get(id)));

		return List.of();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.SERIALIZER;
	}

	public static class Serializer implements CustomIngredientSerializer<SimpleDatagenIngredient> {
		public static final Serializer SERIALIZER = new Serializer();

		public static final ResourceLocation ID = Create.asResource("simple_datagen");
		public static final MapCodec<SimpleDatagenIngredient> CODEC = SimpleDatagenIngredient.CODEC;
		public static final StreamCodec<RegistryFriendlyByteBuf, SimpleDatagenIngredient> STREAM_CODEC = StreamCodec.composite(
			Mods.STREAM_CODEC, ingredient -> ingredient.mod,
			ByteBufCodecs.STRING_UTF8, ingredient -> ingredient.id,
			SimpleDatagenIngredient::new
		);

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<SimpleDatagenIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SimpleDatagenIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}
	}

	/*@Override
	public @NotNull IngredientType<?> getType() {
		if (!didRegistryInjection) {
			// Need to do some registry injection to get the Registry#byNameCodec to encode the right type for this
			// getResourceKey and getId
			// byValue and toId
			// Holder.Reference: key
			if (NeoForgeRegistries.INGREDIENT_TYPES instanceof MappedRegistryAccessor<?> mra) {
				@SuppressWarnings("unchecked")
				MappedRegistryAccessor<IngredientType<?>> mra$ = (MappedRegistryAccessor<IngredientType<?>>) mra;

				IngredientType<?> baseType = NeoForgeMod.COMPOUND_INGREDIENT_TYPE.get();

				int wrappedId = mra$.getToId().getOrDefault(baseType, -1);
				ResourceKey<IngredientType<?>> wrappedKey = NeoForgeMod.COMPOUND_INGREDIENT_TYPE.getKey();

				mra$.getToId().put(INGREDIENT_TYPE, wrappedId);
				//noinspection DataFlowIssue - it is ok to pass null as the owner, because this is only being used for serialization
				mra$.getByValue().put(INGREDIENT_TYPE, Holder.Reference.createStandAlone(null, wrappedKey));

				/*
				{
					"type": "neoforge:compound",
					"ingredients": [
						{
							"item": "mod:compat_item"
						}
					]
				}
				 *//*

				didRegistryInjection = true;
			} else {
				throw new AssertionError("SimpleDatagenIngredient will not be able to" +
						" serialize without injecting into a registry. Expected" +
						" NeoForgeRegistries.INGREDIENT_TYPES to be of class MappedRegistry, is of class " +
						NeoForgeRegistries.INGREDIENT_TYPES.getClass()
				);
			}
		}
		return INGREDIENT_TYPE;
	}*/

}
