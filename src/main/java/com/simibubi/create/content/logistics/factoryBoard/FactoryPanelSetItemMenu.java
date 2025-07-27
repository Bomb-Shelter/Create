package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.simibubi.create.foundation.utility.CreateLang;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class FactoryPanelSetItemMenu extends GhostItemMenu<FactoryPanelBehaviour, FactoryPanelPosition> {

	public FactoryPanelSetItemMenu(MenuType<?> type, int id, Inventory inv, FactoryPanelBehaviour contentHolder) {
		super(type, id, inv, contentHolder);
	}

	public FactoryPanelSetItemMenu(MenuType<?> type, int id, Inventory inv, FactoryPanelPosition extraData) {
		super(type, id, inv, extraData, true);
	}

	public static FactoryPanelSetItemMenu create(int id, Inventory inv, FactoryPanelBehaviour be) {
		return new FactoryPanelSetItemMenu(AllMenuTypes.FACTORY_PANEL_SET_ITEM.get(), id, inv, be);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(1);
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected FactoryPanelBehaviour createOnClient(FactoryPanelPosition pos) {
		return FactoryPanelBehaviour.at(Minecraft.getInstance().level, pos);
	}

	@Override
	protected void addSlots() {
		int playerX = 13;
		int playerY = 112;
		int slotX = 74;
		int slotY = 28;

		addPlayerSlots(playerX, playerY);
		addSlot(new SlotItemHandler(ghostInventory, 0, slotX, slotY));
	}

	@Override
	protected void saveData(FactoryPanelBehaviour contentHolder) {
		if (!contentHolder.setFilter(ghostInventory.getStackInSlot(0))) {
			player.displayClientMessage(CreateLang.translateDirect("logistics.filter.invalid_item"), true);
			AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
			return;
		}
		player.level()
			.playSound(null, contentHolder.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
	}

}
