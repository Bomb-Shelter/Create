package com.simibubi.create.content.logistics.packager;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.packager.InventoryIdentifier;

/**
 * An item inventory, possibly with an associated InventoryIdentifier.
 */
public record IdentifiedInventory(@Nullable InventoryIdentifier identifier, Storage<ItemVariant> handler) {
}
