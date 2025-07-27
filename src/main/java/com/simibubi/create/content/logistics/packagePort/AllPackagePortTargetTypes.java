package com.simibubi.create.content.logistics.packagePort;

import io.github.fabricators_of_create.porting_lib.registry.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.TrainStationFrogportTarget;

import net.minecraft.core.Holder;

public class AllPackagePortTargetTypes {
	private static final DeferredRegister<PackagePortTargetType> REGISTER = DeferredRegister.create(CreateRegistries.PACKAGE_PORT_TARGET_TYPE, Create.ID);

	public static final Holder<PackagePortTargetType> CHAIN_CONVEYOR = REGISTER.register("chain_conveyor", ChainConveyorFrogportTarget.Type::new);
	public static final Holder<PackagePortTargetType> TRAIN_STATION = REGISTER.register("train_station", TrainStationFrogportTarget.Type::new);

	@Internal
	public static void register() {
		REGISTER.register();
	}
}
