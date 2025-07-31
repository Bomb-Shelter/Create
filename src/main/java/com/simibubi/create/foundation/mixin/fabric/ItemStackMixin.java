package com.simibubi.create.foundation.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import com.simibubi.create.foundation.events.ClientEvents;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@ModifyReturnValue(method = "getTooltipLines", at = @At(value = "RETURN", ordinal = 1))
	private List<Component> addCreateTooltip(List<Component> original, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		ClientEvents.addToItemTooltip((ItemStack) (Object) this, tooltipContext, tooltipFlag, original, player);
		return original;
	}
}
