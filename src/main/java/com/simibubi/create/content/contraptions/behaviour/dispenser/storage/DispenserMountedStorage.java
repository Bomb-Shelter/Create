package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import java.util.function.Consumer;
import java.util.function.Predicate;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.menu.MountedStorageMenus;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DispenserMountedStorage extends SimpleMountedStorage {
	public static final MapCodec<DispenserMountedStorage> CODEC = SimpleMountedStorage.codec(DispenserMountedStorage::new);

	protected DispenserMountedStorage(MountedItemStorageType<?> type, SlottedStackStorage handler) {
		super(type, handler);
	}

	public DispenserMountedStorage(ItemStackHandler handler) {
		this(AllMountedStorageTypes.DISPENSER.get(), handler);
	}

	public DispenserMountedStorage(SlottedStorage<ItemVariant> storage) {
		this(copyToItemStackHandler(storage));
	}

	@Override
	@Nullable
	protected MenuProvider createMenuProvider(Component name, SlottedStackStorage handler,
											  Predicate<Player> stillValid, Consumer<Player> onClose) {
		return MountedStorageMenus.createGeneric9x9(name, handler, stillValid, onClose);
	}

	@Override
	protected void playOpeningSound(ServerLevel level, Vec3 pos) {
		// dispensers are silent
	}
}
