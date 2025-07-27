package com.simibubi.create.impl.registry;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.registry.CreateRegistries;

public class CreateRegistriesImpl {
	public static void init() {
		DynamicRegistries.registerSynced(
			CreateRegistries.POTATO_PROJECTILE_TYPE,
			PotatoCannonProjectileType.CODEC,
			PotatoCannonProjectileType.CODEC
		);
	}
}
