package com.simibubi.create.foundation.item.render;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class CustomRenderedItems {

	private static final Set<Item> ITEMS = new ReferenceOpenHashSet<>();
	private static boolean itemsFiltered = false;

	/**
	 * Track an item that uses a subclass of {@link CustomRenderedItemModelRenderer} as its custom renderer
	 * to automatically wrap its model with {@link CustomRenderedItemModel}.
	 * @param item The item that should have its model swapped.
	 */
	public static void register(Item item) {
		ITEMS.add(item);
	}

	/**
	 * This method must not be called before item registration is finished!
	 */
	public static void forEach(Consumer<Item> consumer) {
		if (!itemsFiltered) {
			Iterator<Item> iterator = ITEMS.iterator();
			while (iterator.hasNext()) {
				Item item = iterator.next();
				if (BuiltInRegistries.ITEM.getId(item) == -1 || !(BuiltinItemRendererRegistry.INSTANCE.get(item)
					 instanceof CustomRenderedItemModelRenderer)) {
					iterator.remove();
				}
			}
			itemsFiltered = true;
		}
		ITEMS.forEach(consumer);
	}

}
