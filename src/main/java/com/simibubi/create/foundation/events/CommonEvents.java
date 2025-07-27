package com.simibubi.create.foundation.events;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.AllMapDecorationTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.trainmap.TrainMapSync;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.chainConveyor.ServerChainConveyorHandler;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.drill.CobbleGenOptimisation;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.chute.SmartChuteBlockEntity;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerServerHandler;
import com.simibubi.create.content.trains.entity.CarriageEntityHandler;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.foundation.data.RuntimeDataGenerator;
import com.simibubi.create.foundation.map.StationMapDecorationRenderer;
import com.simibubi.create.foundation.pack.DynamicPack;
import com.simibubi.create.foundation.pack.DynamicPackSource;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.TickBasedCache;
import com.simibubi.create.infrastructure.command.AllCommands;

import io.github.fabricators_of_create.porting_lib.conditions.events.AddReloadListenersEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents.EnteringSection;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityJoinLevelEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityLeaveLevelEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.AttackEntityEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerEvents.PlayerLoggedInEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerEvents.PlayerLoggedOutEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerEvents.StartTracking;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent.Pre;
import io.github.fabricators_of_create.porting_lib.event.common.AddPackFindersEvent;
import io.github.fabricators_of_create.porting_lib.gui.map.MapDecorationRendererManager;
import io.github.fabricators_of_create.porting_lib.level.events.LevelEvent;
import io.github.fabricators_of_create.porting_lib.level.events.LevelEvent.Load;
import io.github.fabricators_of_create.porting_lib.level.events.LevelEvent.Unload;
import net.createmod.catnip.data.WorldAttached;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;

public class CommonEvents {
	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
		ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> onChunkUnloaded(chunk));
		ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> onChunkUnloaded(chunk));
		PlayerLoggedInEvent.EVENT.register(CommonEvents::playerLoggedIn);
		PlayerLoggedOutEvent.EVENT.register(CommonEvents::playerLoggedOut);
		ServerTickEvents.END_WORLD_TICK.register(CommonEvents::onServerWorldTick);
		Pre.EVENT.register(CommonEvents::onEntityTick);
		EntityJoinLevelEvent.EVENT.register(CommonEvents::onEntityAdded);
		AttackEntityEvent.EVENT.register(CommonEvents::onEntityAttackedByPlayer);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
		EnteringSection.EVENT.register(CommonEvents::onEntityEnterSection);
		AddReloadListenersEvent.EVENT.register(CommonEvents::addReloadListeners);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> serverStopping());
		Load.EVENT.register(CommonEvents::onLoadWorld);
		Unload.EVENT.register(CommonEvents::onUnloadWorld);
		EntityJoinLevelEvent.EVENT.register(CommonEvents::attachData);
		EntityLeaveLevelEvent.EVENT.register(CommonEvents::onEntityLeaveLevel);
		StartTracking.EVENT.register(CommonEvents::startTracking);
		AddPackFindersEvent.EVENT.register(ModBusEvents::addPackFinders);
		ModBusEvents.onRegisterMapDecorationRenderers();
	}

	public static void onServerTick(MinecraftServer server) {
		Create.SCHEMATIC_RECEIVER.tick();
		Create.LAGGER.tick();
		ServerSpeedProvider.serverTick();
		Create.RAILWAYS.sync.serverTick();
		TrainMapSync.serverTick(server);
		ServerChainConveyorHandler.tick();
		TickBasedCache.tick();
	}

	public static void onChunkUnloaded(LevelChunk chunk) {
		CapabilityMinecartController.onChunkUnloaded(chunk.getLevel(), chunk);
	}

	public static void playerLoggedIn(PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		ToolboxHandler.playerLogin(player);
		Create.RAILWAYS.playerLogin(player);
	}

	public static void playerLoggedOut(PlayerLoggedOutEvent event) {
		Player player = event.getEntity();
		Create.RAILWAYS.playerLogout(player);
	}

	public static void onServerWorldTick(Level world) {
		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);
		LinkedControllerServerHandler.tick(world);
		ControlsServerHandler.tick(world);
		Create.RAILWAYS.tick(world);
		Create.LOGISTICS.tick(world);
	}

	public static void onEntityTick(EntityTickEvent.Pre event) {
		CapabilityMinecartController.entityTick(event);

		if (event.getEntity() instanceof LivingEntity livingEntity) {
			Level level = livingEntity.level();

			ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(livingEntity, level);
			ToolboxHandler.entityTick(livingEntity, level);
		}
	}

	public static void onEntityAdded(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		Level world = event.getLevel();
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
	}

	public static void onEntityAttackedByPlayer(AttackEntityEvent event) {
		WrenchItem.wrenchInstaKillsMinecarts(event);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		AllCommands.register(dispatcher);
	}

	public static void onEntityEnterSection(EnteringSection event) {
		CarriageEntityHandler.onEntityEnterSection(event);
	}

	public static void addReloadListeners(AddReloadListenersEvent event) {
		event.addListener(RecipeFinder.LISTENER);
		event.addListener(BeltHelper.LISTENER);
	}

	public static void serverStopping() {
		Create.SCHEMATIC_RECEIVER.shutdown();
	}

	public static void onLoadWorld(LevelEvent.Load event) {
		LevelAccessor world = event.getLevel();
		Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
		Create.TORQUE_PROPAGATOR.onLoadWorld(world);
		Create.RAILWAYS.levelLoaded(world);
		Create.LOGISTICS.levelLoaded(world);
	}

	public static void onUnloadWorld(LevelEvent.Unload event) {
		LevelAccessor world = event.getLevel();
		Create.REDSTONE_LINK_NETWORK_HANDLER.onUnloadWorld(world);
		Create.TORQUE_PROPAGATOR.onUnloadWorld(world);
		WorldAttached.invalidateWorld(world);
		CobbleGenOptimisation.invalidateWorld(world);
	}

	public static void attachData(EntityJoinLevelEvent event) {
		CapabilityMinecartController.attach(event);
	}

	public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
		if (!event.getEntity()
			.isAlive())
			CapabilityMinecartController.onEntityDeath(event);
	}

	public static void startTracking(PlayerEvents.StartTracking event) {
		CapabilityMinecartController.startTracking(event);
	}

	public static void leftClickEmpty(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}

	public static class ModBusEvents {
		public static void addPackFinders(AddPackFindersEvent event) {
			// Uncomment and rename pack to add built in resource packs
//			if (event.getPackType() == PackType.CLIENT_RESOURCES) {
//				IModFileInfo modFileInfo = ModList.get().getModFileById(Create.ID);
//				if (modFileInfo == null) {
//					Create.LOGGER.error("Could not find Create mod file info; built-in resource packs will be missing!");
//					return;
//				}
//				IModFile modFile = modFileInfo.getFile();
//				event.addRepositorySource(consumer -> {
//                    PackLocationInfo locationInfo = new PackLocationInfo(Create.asResource("legacy_copper").toString(), Component.literal("Create Legacy Copper"), PackSource.BUILT_IN, Optional.empty());
//					PathPackResources.PathResourcesSupplier resourcesSupplier = new PathPackResources.PathResourcesSupplier(modFile.findResource("resourcepacks/legacy_copper"));
//					PackSelectionConfig packSelectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
//					Pack pack = Pack.readMetaAndCreate(locationInfo, resourcesSupplier, PackType.CLIENT_RESOURCES, packSelectionConfig);
//					if (pack != null) {
//						consumer.accept(pack);
//					}
//				});
//			}

			if (event.getPackType() == PackType.SERVER_DATA) {
				DynamicPack dynamicPack = new DynamicPack("create:dynamic_data", PackType.SERVER_DATA);
				RuntimeDataGenerator.insertIntoPack(dynamicPack);
				event.addRepositorySource(new DynamicPackSource("create:dynamic_data", PackType.SERVER_DATA, Pack.Position.BOTTOM, dynamicPack));
			}
		}

		//@net.neoforged.bus.api.SubscribeEvent
		public static void onRegisterMapDecorationRenderers() {
			MapDecorationRendererManager.register(AllMapDecorationTypes.STATION_MAP_DECORATION.value(), new StationMapDecorationRenderer());
		}

		//@net.neoforged.bus.api.SubscribeEvent
		public static void registerCapabilities() {
			ChuteBlockEntity.registerCapabilities();
			SmartChuteBlockEntity.registerCapabilities();
			BeltBlockEntity.registerCapabilities();
			BasinBlockEntity.registerCapabilities();
			BeltTunnelBlockEntity.registerCapabilities();
			BrassTunnelBlockEntity.registerCapabilities();
			CreativeCrateBlockEntity.registerCapabilities();
			CrushingWheelControllerBlockEntity.registerCapabilities();
			ToolboxBlockEntity.registerCapabilities();
			DeployerBlockEntity.registerCapabilities();
			DepotBlockEntity.registerCapabilities();
			PortableFluidInterfaceBlockEntity.registerCapabilities();
			SpoutBlockEntity.registerCapabilities();
			PortableItemInterfaceBlockEntity.registerCapabilities();
			SawBlockEntity.registerCapabilities();
			EjectorBlockEntity.registerCapabilities();
			FluidTankBlockEntity.registerCapabilities();
			CreativeFluidTankBlockEntity.registerCapabilities();
			HosePulleyBlockEntity.registerCapabilities();
			ItemDrainBlockEntity.registerCapabilities();
			ItemVaultBlockEntity.registerCapabilities();
			MechanicalCrafterBlockEntity.registerCapabilities();
			MillstoneBlockEntity.registerCapabilities();
			StressGaugeBlockEntity.registerCapabilities();
			SpeedGaugeBlockEntity.registerCapabilities();
			StationBlockEntity.registerCapabilities();
			SpeedControllerBlockEntity.registerCapabilities();
			SequencedGearshiftBlockEntity.registerCapabilities();
			DisplayLinkBlockEntity.registerCapabilities();
			StockTickerBlockEntity.registerCapabilities();
			PackagerBlockEntity.registerCapabilities();
			RepackagerBlockEntity.registerCapabilities();
			PostboxBlockEntity.registerCapabilities();
			FrogportBlockEntity.registerCapabilities();
		}
	}
}
