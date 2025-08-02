package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import org.jetbrains.annotations.Nullable;

public class DispenserMountedStorageType extends SimpleMountedStorageType<DispenserMountedStorage> {
	public DispenserMountedStorageType() {
		super(DispenserMountedStorage.CODEC);
	}

	@Override
	protected @Nullable SimpleMountedStorage createStorage(SlottedStorage<ItemVariant> storage) {
		try (Transaction t = TransferUtil.getTransaction()) {
			for (SingleSlotStorage<ItemVariant> slot : storage.getSlots()) {
				System.out.println("@@@@@: " + slot.getResource());
			}
		}
		return new DispenserMountedStorage(storage);
	}
}
