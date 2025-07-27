package com.simibubi.create.compat.trinkets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class Trinkets {

	/**
	 * Resolves the Stacks Handler Map given an Entity.
	 * It is recommended to then use a `.map(curiosMap -> curiosMap.get({key})`,
	 * which can be null and would therefore be caught by the Optional::map function.
	 *
	 * @param entity The entity which possibly has a Curio Inventory capability
	 * @return An optional of the Stacks Handler Map
	 */
	private static Optional<Map<String, Map<String, TrinketInventory>>> resolveCuriosMap(LivingEntity entity) {
		return TrinketsApi.getTrinketComponent(entity).map(TrinketComponent::getInventory);
	}

	public static void init() {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> Trinkets::onClientSetup);

		GogglesItem.addIsWearingPredicate(player -> resolveCuriosMap(player)
			.map(curiosMap -> {
				for (Map<String, TrinketInventory> group : curiosMap.values()) {
					for (TrinketInventory stacksHandler : group.values()) {
						// Search all the curio slots for Goggles existing
						int slots = stacksHandler.getContainerSize();
						for (int slot = 0; slot < slots; slot++) {
							if (AllItems.GOGGLES.isIn(stacksHandler.getItem(slot))) {
								return true;
							}
						}
					}
				}

				return false;
			})
			.orElse(false));

		BacktankUtil.addBacktankSupplier(entity -> resolveCuriosMap(entity)
			.map(curiosMap -> {
				List<ItemStack> stacks = new ArrayList<>();
				for (Map<String, TrinketInventory> group : curiosMap.values()) {
					for (TrinketInventory stacksHandler : group.values()) {
						// Search all the curio slots for pressurized air sources, and add them to the list
						int slots = stacksHandler.getContainerSize();
						for (int slot = 0; slot < slots; slot++) {
							final ItemStack itemStack = stacksHandler.getItem(slot);
							if (AllItemTags.PRESSURIZED_AIR_SOURCES.matches(itemStack))
								stacks.add(itemStack);
						}
					}
				}

				return stacks;
			}).orElse(new ArrayList<>()));

	}

	private static void onClientSetup() {
		TrinketsRenderers.register();
	}
}
