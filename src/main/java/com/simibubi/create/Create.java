package com.simibubi.create;

import java.util.Random;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.equipment.bell.HauntedBellPulser;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;
import com.simibubi.create.content.equipment.wrench.WrenchEventHandler;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.fluids.FluidBottleItemHook;
import com.simibubi.create.content.fluids.FluidReactions;
import com.simibubi.create.content.logistics.itemHatch.ItemHatchHandler;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.processing.burner.BlazeBurnerHandler;
import com.simibubi.create.content.redstone.link.LinkHandler;
import com.simibubi.create.content.trains.schedule.ScheduleItemEntityInteraction;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.simibubi.create.foundation.events.CommonEvents;

import com.simibubi.create.impl.registry.CreateRegistriesImpl;

import com.simibubi.create.infrastructure.RemapHelper;
import com.simibubi.create.infrastructure.worldgen.AllBiomeModifiers;

import io.github.fabricators_of_create.porting_lib.milk.PortingLibMilk;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.registrate.CreateRegistrateRegistrationCallback;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.trinkets.Trinkets;
import com.simibubi.create.compat.inventorySorter.InventorySorterCompat;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileBlockHitActions;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.kinetics.TorquePropagator;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.packagePort.AllPackagePortTargetTypes;
import com.simibubi.create.content.logistics.packager.AllInventoryIdentifiers;
import com.simibubi.create.content.logistics.packager.AllUnpackingHandlers;
import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.CreateNBTProcessors;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.recipe.AllIngredients;
import com.simibubi.create.infrastructure.command.ServerLagger;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.data.CreateDatagen;
import com.simibubi.create.infrastructure.worldgen.AllFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacementModifiers;

import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;

public class Create implements ModInitializer {
	public static final String ID = "create";
	public static final String NAME = "Create";

	public static final Logger LOGGER = LogUtils.getLogger();

	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/**
	 * Use the {@link Random} of a local {@link Level} or {@link Entity} or create one
	 */
	@Deprecated
	public static final Random RANDOM = new Random();

	/**
	 * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
	 * {@link CreateRegistrate}.
	 * </br
	 * If you were using this instance to render a callback listener use {@link CreateRegistrateRegistrationCallback#register} instead.
	 */
	private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
		.setTooltipModifierFactory(item ->
			new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
		);

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final GlobalLogisticsManager LOGISTICS = new GlobalLogisticsManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	@Override
	public void onInitialize() {
		onCtor();
	}

	public static void onCtor() {
		LOGGER.info("{} {} initializing! Commit hash: {}", NAME, CreateBuildInfo.VERSION, CreateBuildInfo.GIT_COMMIT);

		//REGISTRATE.registerEventListeners();

		AllSoundEvents.prepare();
		AllTags.init();
		AllCreativeModeTabs.register();
		AllArmorMaterials.register();
		AllDisplaySources.register();
		AllDisplayTargets.register();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
		AllRecipeTypes.register();
		AllParticleTypes.register();
		AllStructureProcessorTypes.register();
		AllEntityDataSerializers.register();
		AllPackets.register();
		AllFeatures.register();
		AllPlacementModifiers.register();
		AllIngredients.register();
		AllAttachmentTypes.register();
		AllDataComponents.register();
		AllMapDecorationTypes.register();
		AllMountedStorageTypes.register();

		AllConfigs.register();

		// TODO - Make these use Registry.register and move them into the RegisterEvent
		AllPackagePortTargetTypes.register();

		AllSchematicStateFilters.registerDefaults();

		// FIXME: some of these registrations are not thread-safe
		BogeySizes.init();
		AllBogeyStyles.init();
		// ----

		ComputerCraftProxy.register();

		PortingLibMilk.enableMilkFluid();
		REGISTRATE.registerEventListeners();

		Create.init();
		Create.onRegister();
		AllEntityTypes.registerEntityAttributes();
		//modEventBus.addListener(EventPriority.HIGHEST, CreateDatagen::gatherDataHighPriority);
		//modEventBus.addListener(EventPriority.LOWEST, CreateDatagen::gatherData);
		AllSoundEvents.register();

		// FIXME: this is not thread-safe
		Mods.TRINKETS.executeIfInstalled(() -> () -> Trinkets.init());
		//Mods.INVENTORYSORTER.executeIfInstalled(() -> () -> InventorySorterCompat.init());

		CommonEvents.init();
		CouplingHandler.init();
		CardboardArmorHandler.init();
		HauntedBellPulser.init();
		SymmetryHandler.init();
		WrenchEventHandler.init();
		ZapperInteractionHandler.init();
		FluidBottleItemHook.init();
		FluidReactions.init();
		ItemHatchHandler.init();
		StockTickerInteractionHandler.init();
		BlazeBurnerHandler.init();
		LinkHandler.init();
		ScheduleItemEntityInteraction.init();
		ValueSettingsInputHandler.init();
		EdgeInteractionHandler.init();
		CreateRegistriesImpl.init();
		AllBiomeModifiers.bootstrap();
		RemapHelper.init();

		for (Runnable callback : CreateBuiltInRegistries.bakeCallbacks) {
			callback.run();
		}
	}

	public static void init() {
		AllFluids.registerFluidInteractions();
		CreateNBTProcessors.register();

		{
			// TODO: custom registration should all happen in one place
			// Most registration happens in the constructor.
			// These registrations use Create's registered objects directly so they must run after registration has finished.
			BoilerHeaters.registerDefaults();
			AllPortalTracks.registerDefaults();
			AllBlockSpoutingBehaviours.registerDefaults();
			AllMovementBehaviours.registerDefaults();
			AllInteractionBehaviours.registerDefaults();
			AllContraptionMovementSettings.registerDefaults();
			AllOpenPipeEffectHandlers.registerDefaults();
			AllMountedDispenseItemBehaviors.registerDefaults();
			AllUnpackingHandlers.registerDefaults();
			AllInventoryIdentifiers.registerDefaults();
			// --
		}
	}

	public static void onRegister() {
		AllArmInteractionPointTypes.init();
		AllFanProcessingTypes.init();
		AllItemAttributeTypes.init();
		AllContraptionTypes.init();
		AllPotatoProjectileRenderModes.init();
		AllPotatoProjectileEntityHitActions.init();
		AllPotatoProjectileBlockHitActions.init();

		/*if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES)*/ {
			AllAdvancements.register();
			AllTriggers.register();
		}
	}

	public static LangBuilder lang() {
		return new LangBuilder(ID);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static CreateRegistrate registrate() {
		if (!STACK_WALKER.getCallerClass().getPackageName().startsWith("com.simibubi.create"))
			throw new UnsupportedOperationException("Other mods are not permitted to use create's registrate instance.");
		return REGISTRATE;
	}
}
