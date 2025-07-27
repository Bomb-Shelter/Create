package com.simibubi.create.compat.trainmap;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.compat.Mods;

import io.github.fabricators_of_create.porting_lib.event.client.MouseInputEvents;
import io.github.fabricators_of_create.porting_lib.event.client.MouseInputEvents.Action;
import io.github.fabricators_of_create.porting_lib.event.client.PreRenderTooltipCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class TrainMapEvents {
	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(TrainMapEvents::tick);
		MouseInputEvents.BEFORE_BUTTON.register(TrainMapEvents::mouseClick);
		PreRenderTooltipCallback.EVENT.register((stack, poseStack, x, y, screenWidth, screenHeight, font, components) -> cancelTooltips());
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, tickDelta) -> {
				renderGui(screen1, drawContext, mouseX, mouseY);
			});
		});
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null)
			return;

		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.tick();
		if (Mods.JOURNEYMAP.isLoaded())
			JourneyTrainMap.tick();
	}

	public static boolean mouseClick(int button, int modifiers, Action action) {
		if (action != MouseInputEvents.Action.PRESS)
			return false;

		boolean value = false;
		if (Mods.FTBCHUNKS.isLoaded() && FTBChunksTrainMap.mouseClick())
			value = true;
		if (Mods.JOURNEYMAP.isLoaded() && JourneyTrainMap.mouseClick())
			value = true;

		return value;
	}

	public static boolean cancelTooltips() {
		if (Mods.FTBCHUNKS.isLoaded())
			return FTBChunksTrainMap.cancelTooltips();

		return false;
	}

	public static void renderGui(Screen screen, GuiGraphics graphics, int oMouseX, int oMouseY) {
		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.renderGui(screen, graphics, oMouseX, oMouseY);
	}

}
