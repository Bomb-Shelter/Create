package com.simibubi.create.compat.trinkets;

import com.simibubi.create.AllItems;

import com.simibubi.create.content.equipment.goggles.GogglesItem;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class TrinketsRenderers {
	public static void register() {
		GogglesItem.addIsWearingPredicate(player -> {
			Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
			if (optional.isPresent()) {
				TrinketComponent component = optional.get();
				if (component.isEquipped(AllItems.GOGGLES.get())) {
					return true;
				}
			}
			return false;
		});

		TrinketRendererRegistry.registerRenderer(AllItems.GOGGLES.get(), new GogglesTrinketsRenderer());
	}
}
