package com.simibubi.create.infrastructure.fabric.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import com.simibubi.create.Create;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Ingredient} that matches {@link ItemStack}s of {@link Block}s from a {@link TagKey<Block>}, useful in cases
 * like {@code "minecraft:convertable_to_mud"} where there isn't an accompanying item tag
 * <p>
 * Notice: This should not be used as a replacement for the normal {@link Ingredient#of(TagKey)},
 * This should only be used when there is no way an item tag can be used in your use case
 */
public class BlockTagIngredient implements CustomIngredient {
	public static final Codec<BlockTagIngredient> CODEC = TagKey.codec(Registries.BLOCK)
		.xmap(BlockTagIngredient::new, BlockTagIngredient::getTag);
	public static final MapCodec<BlockTagIngredient> MAP_CODEC = CODEC.fieldOf("tag");

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

	protected final TagKey<Block> tag;

	@Nullable
	protected ItemStack[] itemStacks;

	public BlockTagIngredient(TagKey<Block> tag) {
		this.tag = tag;
	}

	protected void dissolve() {
		if (itemStacks == null) {
			List<ItemStack> list = new ArrayList<>();
			for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
				ItemStack stack = new ItemStack(block.value());
				if (!stack.isEmpty()) {
					list.add(stack);
				}
			}

			if (list.isEmpty()) {
				ItemStack itemStack = new ItemStack(Blocks.BARRIER);
				itemStack.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + this.tag.location()));
				list.add(itemStack);
			}

			itemStacks = list.toArray(ItemStack[]::new);
		}
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		if (stack == null)
			return false;

		dissolve();
		for (ItemStack itemStack : itemStacks) {
			if (itemStack.is(stack.getItem())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		dissolve();
		return Arrays.stream(itemStacks).toList();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return BlockTagIngredientSerializer.SERIALIZER;
	}

	public TagKey<Block> getTag() {
		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockTagIngredient that)) return false;
		return tag.equals(that.tag);
	}

	@Override
	public int hashCode() {
		return tag.hashCode();
	}

	public static class BlockTagIngredientSerializer implements CustomIngredientSerializer<BlockTagIngredient> {
		public static final ResourceLocation ID = Create.asResource("block_tag");
		public static final BlockTagIngredientSerializer SERIALIZER = new BlockTagIngredientSerializer();

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<BlockTagIngredient> getCodec(boolean allowEmpty) {
			return BlockTagIngredient.MAP_CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> getPacketCodec() {
			return BlockTagIngredient.STREAM_CODEC;
		}
	}
}

