package com.simibubi.create.foundation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.item.render.CustomItemModels;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;

import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelSwapper {

	protected CustomBlockModels customBlockModels = new CustomBlockModels();
	protected CustomItemModels customItemModels = new CustomItemModels();

	private Map<ModelResourceLocation, NonNullFunction<BakedModel, ? extends BakedModel>> swaps = null;

	public CustomBlockModels getCustomBlockModels() {
		return customBlockModels;
	}

	public CustomItemModels getCustomItemModels() {
		return customItemModels;
	}

	public void registerListeners() {
		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.modifyModelAfterBake().register((model, context) -> {
				if (swaps == null) {
					this.swaps = new HashMap<>();

					customBlockModels.forEach((block, modelFunc) -> getAllBlockStateModelLocations(block).forEach(id -> swaps.put(id, modelFunc)));
					customItemModels.forEach((item, modelFunc) -> swaps.put(getItemModelLocation(item), modelFunc));
					CustomRenderedItems.forEach(item -> swaps.put(getItemModelLocation(item), CustomRenderedItemModel::new));
				}

				if (context.topLevelId() != null) {
					var swap = swaps.get(context.topLevelId());
					return swap != null ? swap.apply(model) : model;
				} else if (context.resourceId() != null) {
					for (Entry<ModelResourceLocation, NonNullFunction<BakedModel, ? extends BakedModel>> entry : swaps.entrySet()) {
						if (entry.getKey().id().equals(context.resourceId())) {
							return entry.getValue() != null ? entry.getValue().apply(model) : model;
						}
					}
				}

				return model;
			});
		});
	}

	public static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		ResourceLocation blockRl = RegisteredObjectsHelper.getKeyOrThrow(block);
		block.getStateDefinition()
			.getPossibleStates()
			.forEach(state -> {
				models.add(BlockModelShaper.stateToModelLocation(blockRl, state));
			});
		return models;
	}

	public static ModelResourceLocation getItemModelLocation(Item item) {
		return new ModelResourceLocation(RegisteredObjectsHelper.getKeyOrThrow(item), "inventory");
	}

}
