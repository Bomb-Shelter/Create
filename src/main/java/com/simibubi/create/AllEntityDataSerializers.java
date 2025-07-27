package com.simibubi.create;

import com.simibubi.create.content.trains.entity.CarriageSyncDataSerializer;

import io.github.fabricators_of_create.porting_lib.registry.DeferredHolder;
import io.github.fabricators_of_create.porting_lib.registry.DeferredRegister;
import net.minecraft.network.syncher.EntityDataSerializer;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllEntityDataSerializers {
	// Fabric TODO: figure this out probably
	/*private static final DeferredRegister<EntityDataSerializer<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Create.ID);

	public static final CarriageSyncDataSerializer CARRIAGE_DATA = new CarriageSyncDataSerializer();

	public static final DeferredHolder<EntityDataSerializer<?>, CarriageSyncDataSerializer> CARRIAGE_DATA_ENTRY = REGISTER.register("carriage_data", () -> CARRIAGE_DATA);
*/
	@Internal
	public static void register() {
		//REGISTER.register();
	}
}
