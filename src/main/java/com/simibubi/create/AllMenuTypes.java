package com.simibubi.create;

import com.simibubi.create.content.equipment.blueprint.BlueprintEntity.BlueprintData;
import com.simibubi.create.content.equipment.blueprint.BlueprintMenu;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxScreen;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import com.simibubi.create.content.logistics.filter.PackageFilterMenu;
import com.simibubi.create.content.logistics.filter.PackageFilterScreen;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity.StockTickerData;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerMenu;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.schematics.cannon.SchematicannonMenu;
import com.simibubi.create.content.schematics.cannon.SchematicannonScreen;
import com.simibubi.create.content.schematics.table.SchematicTableMenu;
import com.simibubi.create.content.schematics.table.SchematicTableScreen;
import com.simibubi.create.content.trains.schedule.ScheduleMenu;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity.SmartBlockData;
import com.tterrag.registrate.builders.MenuBuilder.FabricMenuFactory;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class AllMenuTypes {

	public static final MenuEntry<SchematicTableMenu> SCHEMATIC_TABLE =
		register("schematic_table", SchematicTableMenu::new, () -> SchematicTableScreen::new, SmartBlockData.STREAM_CODEC);

	public static final MenuEntry<SchematicannonMenu> SCHEMATICANNON =
		register("schematicannon", SchematicannonMenu::new, () -> SchematicannonScreen::new, SmartBlockData.STREAM_CODEC);

	public static final MenuEntry<FilterMenu> FILTER =
		register("filter", FilterMenu::new, () -> FilterScreen::new, ItemStack.STREAM_CODEC);

	public static final MenuEntry<AttributeFilterMenu> ATTRIBUTE_FILTER =
		register("attribute_filter", AttributeFilterMenu::new, () -> AttributeFilterScreen::new, ItemStack.STREAM_CODEC);

	public static final MenuEntry<PackageFilterMenu> PACKAGE_FILTER =
		register("package_filter", PackageFilterMenu::new, () -> PackageFilterScreen::new, ItemStack.STREAM_CODEC);

	public static final MenuEntry<BlueprintMenu> CRAFTING_BLUEPRINT =
		register("crafting_blueprint", BlueprintMenu::new, () -> BlueprintScreen::new, BlueprintData.STREAM_CODEC);

	public static final MenuEntry<LinkedControllerMenu> LINKED_CONTROLLER =
		register("linked_controller", LinkedControllerMenu::new, () -> LinkedControllerScreen::new, ItemStack.STREAM_CODEC);

	public static final MenuEntry<ToolboxMenu> TOOLBOX =
		register("toolbox", ToolboxMenu::new, () -> ToolboxScreen::new, SmartBlockData.STREAM_CODEC);

	public static final MenuEntry<ScheduleMenu> SCHEDULE =
		register("schedule", ScheduleMenu::new, () -> ScheduleScreen::new, ItemStack.STREAM_CODEC);

	public static final MenuEntry<StockKeeperCategoryMenu> STOCK_KEEPER_CATEGORY =
		register("stock_keeper_category", StockKeeperCategoryMenu::new, () -> StockKeeperCategoryScreen::new, BlockPos.STREAM_CODEC);

	public static final MenuEntry<StockKeeperRequestMenu> STOCK_KEEPER_REQUEST =
		register("stock_keeper_request", StockKeeperRequestMenu::new, () -> StockKeeperRequestScreen::new, StockTickerData.STREAM_CODEC);

	public static final MenuEntry<PackagePortMenu> PACKAGE_PORT =
		register("package_port", PackagePortMenu::new, () -> PackagePortScreen::new, BlockPos.STREAM_CODEC);

	public static final MenuEntry<RedstoneRequesterMenu> REDSTONE_REQUESTER =
		register("redstone_requester", RedstoneRequesterMenu::new, () -> RedstoneRequesterScreen::new, BlockPos.STREAM_CODEC);

	public static final MenuEntry<FactoryPanelSetItemMenu> FACTORY_PANEL_SET_ITEM =
		register("factory_panel_set_item", FactoryPanelSetItemMenu::new, () -> FactoryPanelSetItemScreen::new, FactoryPanelPosition.STREAM_CODEC);

	private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>, D> MenuEntry<C> register(
		String name, FabricMenuFactory<C, D> factory, NonNullSupplier<ScreenFactory<C, S>> screenFactory, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec) {
		return Create.registrate()
			.menu(name, factory, screenFactory, (StreamCodec<RegistryFriendlyByteBuf, D>) streamCodec)
			.register();
	}

	public static void register() {
	}

}
