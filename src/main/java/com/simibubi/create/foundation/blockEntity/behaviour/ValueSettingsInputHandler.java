package com.simibubi.create.foundation.blockEntity.behaviour;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.AdventureUtil;

import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.RightClickBlock;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.entity.FakePlayer;

public class ValueSettingsInputHandler {
	public static void init() {
		RightClickBlock.EVENT.register(ValueSettingsInputHandler::onBlockActivated);
	}

	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		Level world = event.getLevel();
		BlockPos pos = event.getPos();
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();

		if (!canInteract(player))
			return;
		if (AllBlocks.CLIPBOARD.isIn(player.getMainHandItem()))
			return;
		if (!(world.getBlockEntity(pos)instanceof SmartBlockEntity sbe))
			return;

		if (event.getSide() == EnvType.CLIENT)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CreateClient.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(event));

		if (event.isCanceled())
			return;

		for (BlockEntityBehaviour behaviour : sbe.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;
			if (valueSettingsBehaviour.bypassesInput(player.getMainHandItem()))
				continue;
			if (!valueSettingsBehaviour.mayInteract(player))
				continue;

			BlockHitResult ray = event.getHitVec();
			if (ray == null)
				return;
			if (behaviour instanceof SidedFilteringBehaviour) {
				behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.getDirection());
				if (behaviour == null)
					continue;
			}

			if (!valueSettingsBehaviour.isActive())
				continue;
			if (valueSettingsBehaviour.onlyVisibleWithWrench()
				&& !AllItemTags.WRENCH.matches(player.getItemInHand(hand)))
				continue;
			if (valueSettingsBehaviour.getSlotPositioning()instanceof ValueBoxTransform.Sided sidedSlot) {
				if (!sidedSlot.isSideActive(sbe.getBlockState(), ray.getDirection()))
					continue;
				sidedSlot.fromSide(ray.getDirection());
			}

			boolean fakePlayer = player instanceof FakePlayer;
			if (!valueSettingsBehaviour.testHit(ray.getLocation()) && !fakePlayer)
				continue;

			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);

			if (!valueSettingsBehaviour.acceptsValueSettings() || fakePlayer) {
				valueSettingsBehaviour.onShortInteract(player, hand, ray.getDirection(), ray);
				return;
			}

			if (event.getSide() == EnvType.CLIENT) {
				BehaviourType<?> type = behaviour.getType();
				CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CreateClient.VALUE_SETTINGS_HANDLER
					.startInteractionWith(pos, type, hand, ray.getDirection()));
			}

			return;
		}
	}

	public static boolean canInteract(Player player) {
		return player != null && !player.isSpectator() && !player.isShiftKeyDown() && !AdventureUtil.isAdventure(player);
	}
}
