package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity.StockTickerData;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity.SmartBlockData;
import com.simibubi.create.foundation.gui.menu.MenuBase;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class StockKeeperRequestMenu extends MenuBase<StockTickerBlockEntity, StockTickerData> {

	boolean isAdmin;
	boolean isLocked;

	public Object screenReference;

	public StockKeeperRequestMenu(MenuType<?> type, int id, Inventory inv, StockTickerData extraData) {
		super(type, id, inv, extraData, true);
	}

	public StockKeeperRequestMenu(MenuType<?> type, int id, Inventory inv, StockTickerBlockEntity contentHolder) {
		super(type, id, inv, contentHolder);
	}

	public static AbstractContainerMenu create(int pContainerId, Inventory pPlayerInventory,
		StockTickerBlockEntity stockTickerBlockEntity) {
		return new StockKeeperCategoryMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), pContainerId, pPlayerInventory,
			stockTickerBlockEntity);
	}

	@Override
	protected StockTickerBlockEntity createOnClient(StockTickerData extraData) {
		isAdmin = extraData.showLockOption();
		isLocked = extraData.isCurrentlyLocked();
		if (Minecraft.getInstance().level
			.getBlockEntity(extraData.targetPos()) instanceof StockTickerBlockEntity stbe)
			return stbe;
		return null;
	}

	@Override
	protected void initAndReadInventory(StockTickerBlockEntity contentHolder) {}

	@Override
	public void initializeContents(int pStateId, List<ItemStack> pItems, ItemStack pCarried) {}

	@Override
	protected void addSlots() {
		addPlayerSlots(-1000, 0);
	}

	@Override
	protected void saveData(StockTickerBlockEntity contentHolder) {}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

}
