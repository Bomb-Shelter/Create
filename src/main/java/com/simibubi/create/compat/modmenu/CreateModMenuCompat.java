package com.simibubi.create.compat.modmenu;

import com.simibubi.create.Create;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.createmod.catnip.config.ui.BaseConfigScreen;

public class CreateModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> new BaseConfigScreen(screen, Create.ID);
	}
}
