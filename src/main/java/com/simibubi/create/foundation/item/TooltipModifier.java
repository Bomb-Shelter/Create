package com.simibubi.create.foundation.item;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.world.item.Item;

import java.util.List;

@FunctionalInterface
public interface TooltipModifier {
	SimpleRegistry<Item, TooltipModifier> REGISTRY = SimpleRegistry.create();

	TooltipModifier EMPTY = new TooltipModifier() {
		@Override
		public void modify(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> tooltip, Player player) {
		}

		@Override
		public TooltipModifier andThen(TooltipModifier after) {
			return after;
		}
	};

	void modify(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> tooltip, Player player);

	default TooltipModifier andThen(TooltipModifier after) {
		if (after == EMPTY) {
			return this;
		}
		return (stack, context, flag, tooltip, player) -> {
			modify(stack, context, flag, tooltip, player);
			after.modify(stack, context, flag, tooltip, player);
		};
	}

	static TooltipModifier mapNull(@Nullable TooltipModifier modifier) {
		if (modifier == null) {
			return EMPTY;
		}
		return modifier;
	}
}
