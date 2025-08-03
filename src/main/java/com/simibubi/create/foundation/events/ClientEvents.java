package com.simibubi.create.foundation.events;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.simibubi.create.content.contraptions.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.minecart.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.CouplingRenderer;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfoManager;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import com.simibubi.create.content.decoration.girder.GirderWrenchBehavior;
import com.simibubi.create.content.equipment.armor.BacktankArmorLayer;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.equipment.hats.CreateHatArmorLayer;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItemRenderer;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.equipment.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorInteractionHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.kinetics.turntable.TurntableHandler;
import com.simibubi.create.content.logistics.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import com.simibubi.create.content.logistics.tableCloth.TableClothOverlayRenderer;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageCouplingRenderer;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.simibubi.create.content.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackPlacement;
import com.simibubi.create.content.trains.track.TrackPlacementOverlay;
import com.simibubi.create.content.trains.track.TrackTargetingClient;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import com.simibubi.create.foundation.events.fabric.ChunkUnloadCallback;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.TickBasedCache;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.github.fabricators_of_create.porting_lib.client_events.event.client.ViewportEvent;
import io.github.fabricators_of_create.porting_lib.client_events.event.client.ViewportEvent.ComputeCameraAngles;
import io.github.fabricators_of_create.porting_lib.client_events.event.client.ViewportEvent.RenderFog;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityMountEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.LeftClickEmpty;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback.Types;
import io.github.fabricators_of_create.porting_lib.item.client.IItemDecorator;
import io.github.fabricators_of_create.porting_lib.item.client.callbacks.ItemDecorationsCallback;
import io.github.fabricators_of_create.porting_lib.level.events.LevelEvent;
import io.github.fabricators_of_create.porting_lib.level.events.LevelEvent.Load;
import io.github.fabricators_of_create.porting_lib.level.events.LevelEvent.Unload;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class ClientEvents {
	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> onTickPre());
		ClientTickEvents.END_CLIENT_TICK.register(client -> onTickPost());
		ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> ChunkUnloadCallback.EVENT.invoker().onChunkUnload(world, chunk));
	}

	public static void onTickPre() {
		onTick( true);
	}

	public static void onTickPost() {
		onTick(false);
	}

	public static void onTick(boolean isPreEvent) {
		if (!isGameActive())
			return;

		Level world = Minecraft.getInstance().level;
		if (isPreEvent) {
			LinkedControllerClientHandler.tick();
			ControlsHandler.tick();
			AirCurrent.Client.tickClientPlayerSounds();
			return;
		}

		SoundScapes.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.GLUE_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);
		CreateClient.RAILWAYS.clientTick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
//		BeltSlicer.tickHoveringInformation();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		GirderWrenchBehavior.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		ContraptionRenderInfoManager.tickFor(world);
		BlueprintOverlayRenderer.tick();
		ToolboxHandlerClient.clientTick();
		RadialWrenchHandler.clientTick();
		TrackTargetingClient.clientTick();
		TrackPlacement.clientTick();
		TrainRelocator.clientTick();
		ClickToLinkBlockItem.clientTick();
		CurvedTrackInteraction.clientTick();
		CameraDistanceModifier.tick();
		CameraAngleAnimationService.tick();
		TrainHUD.tick();
		ClipboardValueSettingsHandler.clientTick();
		CreateClient.VALUE_SETTINGS_HANDLER.tick();
		ScrollValueHandler.tick();
		NetheriteBacktankFirstPersonRenderer.clientTick();
		ContraptionPlayerPassengerRotation.tick();
		ChainConveyorInteractionHandler.clientTick();
		ChainConveyorRidingHandler.clientTick();
		ChainConveyorConnectionHandler.clientTick();
		PackagePortTargetSelectionHandler.tick();
		LogisticallyLinkedClientHandler.tick();
		TableClothOverlayRenderer.tick();
		CardboardArmorStealthOverlay.clientTick();
		FactoryPanelConnectionHandler.clientTick();
		TickBasedCache.clientTick();
		// fabric: see comment
		AllKeys.fixBinds();
	}

	static {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoin());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onLeave());
		Load.EVENT.register(ClientEvents::onLoadWorld);
		Unload.EVENT.register(ClientEvents::onUnloadWorld);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ClientEvents::onRenderWorld);
		ComputeCameraAngles.EVENT.register(ClientEvents::onCameraSetup);
		ClientTickEvents.START_CLIENT_TICK.register(client -> onRenderFramePre());
		ClientTickEvents.END_CLIENT_TICK.register(client -> onRenderFramePost());
		EntityMountEvent.EVENT.register(ClientEvents::onMount);
		RenderFog.EVENT.register(ClientEvents::getFogDensity);
		LeftClickEmpty.EVENT.register(ClientEvents::leftClickEmpty);

		ModBusEvents.init();
	}

	public static void onJoin() {
		CreateClient.checkGraphicsFanciness();
	}

	public static void onLeave() {
		CreateClient.RAILWAYS.cleanUp();
	}

	public static void onLoadWorld(LevelEvent.Load event) {
		LevelAccessor world = event.getLevel();
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientLevel)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	public static void onUnloadWorld(LevelEvent.Unload event) {
		if (!event.getLevel()
			.isClientSide())
			return;
		CreateClient.invalidateRenderers();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
		AnimationTickHolder.reset();
		ControlsHandler.levelUnloaded(event.getLevel());
	}

	public static void onRenderWorld(WorldRenderContext context) {
		PoseStack ms = context.matrixStack();
		ms.pushPose();
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();

		TrackBlockOutline.drawCurveSelection(ms, buffer, camera);
		TrackTargetingClient.render(ms, buffer, camera);
		CouplingRenderer.renderAll(ms, buffer, camera);
		CarriageCouplingRenderer.renderAll(ms, buffer, camera);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer, camera);
		ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, camera);

		buffer.draw();
		RenderSystem.enableCull();
		ms.popPose();

		ContraptionPlayerPassengerRotation.frame();
	}

	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		if (CameraAngleAnimationService.isYawAnimating())
			event.setYaw(CameraAngleAnimationService.getYaw(partialTicks));

		if (CameraAngleAnimationService.isPitchAnimating())
			event.setPitch(CameraAngleAnimationService.getPitch(partialTicks));
	}

	public static void addToItemTooltip(ItemStack stack, TooltipContext context, TooltipFlag type, List<Component> lines, @Nullable Player player) {
		if (!AllConfigs.client().tooltips.get())
			return;
		if (player == null)
			return;

		Item item = stack.getItem();
		TooltipModifier modifier = TooltipModifier.REGISTRY.get(item);
		if (modifier != null && modifier != TooltipModifier.EMPTY) {
			modifier.modify(stack, context, type, lines, player);
		}

		SequencedAssemblyRecipe.addToTooltip(stack, lines);
	}

	public static void onRenderFramePre() {
		onRenderFrame(true);
	}

	public static void onRenderFramePost() {
		onRenderFrame(false);
	}

	public static void onRenderFrame(boolean isPreEvent) {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderFrame();
	}

	public static void onMount(EntityMountEvent event) {
		if (event.getEntityMounting() != Minecraft.getInstance().player)
			return;

		if (event.isDismounting()) {
			CameraDistanceModifier.reset();
			return;
		}

		if (!event.isMounting() || !(event.getEntityBeingMounted() instanceof CarriageContraptionEntity carriage)) {
			return;
		}

		CameraDistanceModifier.zoomOut();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static void getFogDensity(ViewportEvent.RenderFog event) {
		Camera camera = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = camera.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (camera.getPosition().y >= blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();
		Entity entity = camera.getEntity();

		if (entity.isSpectator())
			return;

		ItemStack divingHelmet = DivingHelmetItem.getWornItem(entity);
		if (!divingHelmet.isEmpty()) {
			if (FluidHelper.isWater(fluid)) {
				event.scaleFarPlaneDistance(6.25f);
				event.setCanceled(true);
				return;
			} else if (FluidHelper.isLava(fluid) && NetheriteDivingHandler.isNetheriteDivingHelmet(divingHelmet)) {
				event.setNearPlaneDistance(-4.0f);
				event.setFarPlaneDistance(20.0f);
				event.setCanceled(true);
				return;
			}
		}
	}

	public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof ZapperItem) {
			CatnipServices.NETWORK.sendToServer(LeftClickPacket.INSTANCE);
		}
	}

	public static class ModBusEvents {
		public static void init() {
			registerClientReloadListeners();
			addEntityRendererLayers();
			registerGuiOverlays();

			ItemDecorationsCallback.EVENT.register(ModBusEvents::registerItemDecorations);
		}

		public static void registerClientReloadListeners() {
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(CreateClient.RESOURCE_RELOAD_LISTENER);
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(TrainHatInfoReloadListener.LISTENER);
		}

		public static void addEntityRendererLayers() {
			LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
				BacktankArmorLayer.registerOn(entityRenderer, registrationHelper);
				CreateHatArmorLayer.registerOn(entityRenderer, registrationHelper);
			});
		}

		public static void registerGuiOverlays() {
			// Register overlays in reverse order
			OverlayRenderCallback.EVENT.register((guiGraphics, partialTicks, window, type) -> {
				var timer = Minecraft.getInstance().getTimer();

				if (type == Types.AIR) {
					RemainingAirOverlay.INSTANCE.render(guiGraphics, timer);
				} else if (type == Types.CROSSHAIRS) {
					TrainHUD.OVERLAY.render(guiGraphics, timer);

					CreateClient.VALUE_SETTINGS_HANDLER.render(guiGraphics, timer);
					TrackPlacementOverlay.INSTANCE.render(guiGraphics, timer);
					GoggleOverlayRenderer.OVERLAY.render(guiGraphics, timer);
					BlueprintOverlayRenderer.OVERLAY.render(guiGraphics, timer);
					LinkedControllerClientHandler.OVERLAY.render(guiGraphics, timer);
					CreateClient.SCHEMATIC_HANDLER.render(guiGraphics, timer);
					ToolboxHandlerClient.OVERLAY.render(guiGraphics, timer);
				}

				return false;
			});
		}

		public static void registerItemDecorations(Map<Item, List<IItemDecorator>> decorators) {
			decorators.put(AllItems.POTATO_CANNON.get(), List.of(PotatoCannonItemRenderer.DECORATOR));
		}

		/*@SubscribeEvent
		public static void onLoadComplete(FMLLoadCompleteEvent event) {
			ModContainer createContainer = ModList.get()
				.getModContainerById(Create.ID)
				.orElseThrow(() -> new IllegalStateException("Create mod container missing on LoadComplete"));
			Supplier<IConfigScreenFactory> configScreen = () -> (mc, previousScreen) -> new BaseConfigScreen(previousScreen, Create.ID);
			createContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
		}*/

	}
}
