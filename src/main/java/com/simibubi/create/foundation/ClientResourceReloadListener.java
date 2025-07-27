package com.simibubi.create.foundation;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.logistics.tableCloth.TableClothModel;
import com.simibubi.create.foundation.sound.SoundScapes;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
	public static final ResourceLocation ID = Create.asResource("client_resources");

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		CreateClient.invalidateRenderers();
		SoundScapes.invalidateAll();
		BeltHelper.uprightCache.clear();
		TableClothModel.reload();
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}
}
