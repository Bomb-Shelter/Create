package com.simibubi.create.compat.sodium;

import java.util.function.Function;

import com.simibubi.create.Create;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
public class SodiumCompat {
	public static final ResourceLocation SAW_TEXTURE = Create.asResource("block/saw_reversed");
	public static final ResourceLocation FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");

	public static void init() {
		Minecraft mc = Minecraft.getInstance();
		WorldRenderEvents.START.register(context -> {
			{
				Function<ResourceLocation, TextureAtlasSprite> atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
				TextureAtlasSprite sawSprite = atlas.apply(SAW_TEXTURE);
				SpriteUtil.INSTANCE.markSpriteActive(sawSprite);

				TextureAtlasSprite factoryPanelSprite = atlas.apply(FACTORY_PANEL_TEXTURE);
				SpriteUtil.INSTANCE.markSpriteActive(factoryPanelSprite);
			}
		});
	}
}
