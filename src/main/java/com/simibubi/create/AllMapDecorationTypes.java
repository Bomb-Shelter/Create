package com.simibubi.create;

import io.github.fabricators_of_create.porting_lib.registry.DeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllMapDecorationTypes {
	private static final DeferredRegister<MapDecorationType> DECORATION_TYPES = DeferredRegister.create(Registries.MAP_DECORATION_TYPE, Create.ID);

	public static final Holder<MapDecorationType> STATION_MAP_DECORATION = DECORATION_TYPES.register("station", () -> new MapDecorationType(Create.asResource("station"), true, -1, false, true));

	@Internal
	public static void register() {
		DECORATION_TYPES.register();
	}
}
