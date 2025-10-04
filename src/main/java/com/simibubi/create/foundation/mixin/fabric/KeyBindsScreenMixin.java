package com.simibubi.create.foundation.mixin.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(KeyBindsScreen.class)
@Implements(value = @Interface(iface = ContainerEventHandler.class, prefix = "kilt$i$"))
public abstract class KeyBindsScreenMixin extends OptionsSubScreen {
	@Shadow
	@Nullable
	public KeyMapping selectedKey;
	@Shadow
	public long lastKeySelection;
	@Shadow
	private KeyBindsList keyBindsList;

	public KeyBindsScreenMixin(Screen lastScreen, Options options, Component title) {
		super(lastScreen, options, title);
	}

	// yes i shamelessly stole this from Kilt, what of it?
	@Intrinsic
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Intrinsic(displace = true) // this still feels like magic to me but oh well
	public boolean kilt$i$keyReleased(int keyCode, int scanCode, int modifiers) {
		// Forge: We wait for a second key above if the first press is a modifier
		// but if they release the modifier then set it explicitly.
		var key = InputConstants.getKey(keyCode, scanCode);
		if (this.selectedKey != null && this.selectedKey.key == key) {
			this.selectedKey = null;
			this.lastKeySelection = Util.getMillis();
			this.keyBindsList.resetMappingAndUpdateButtons();
		}

		return this.keyReleased(keyCode, scanCode, modifiers);
	}
}
