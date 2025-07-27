package com.simibubi.create.content.equipment.armor;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageRenderer;
import com.simibubi.create.foundation.utility.TickBasedCache;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.PlayerTickEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.PlayerTickEvent.Post;
import io.github.fabricators_of_create.porting_lib.event.client.RenderPlayerEvents;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;

public class CardboardArmorHandlerClient {

	private static final Cache<UUID, Integer> BOXES_PLAYERS_ARE_HIDING_AS = new TickBasedCache<>(20, true);

	public static void init() {
		Post.EVENT.register(CardboardArmorHandlerClient::keepCacheAliveDesignDespiteNotRendering);
		RenderPlayerEvents.PRE.register(CardboardArmorHandlerClient::playerRendersAsBoxWhenSneaking);
	}

	public static void keepCacheAliveDesignDespiteNotRendering(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!CardboardArmorHandler.testForStealth(player))
			return;
		try {
			getCurrentBoxIndex(player);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	//@SubscribeEvent(priority = EventPriority.HIGH)
	public static boolean playerRendersAsBoxWhenSneaking(Player player, PlayerRenderer renderer, float partialTick, PoseStack ms, MultiBufferSource bufferSource, int packedLight) {
		if (!CardboardArmorHandler.testForStealth(player))
			return false;

		if (player == Minecraft.getInstance().player
			&& Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON)
			return true;

		ms.pushPose();

		Vec3 renderOffset = renderer.getRenderOffset((AbstractClientPlayer)player, partialTick);
		ms.translate(0, -renderOffset.y, 0);

		float movement = (float) player.position()
			.subtract(player.xo, player.yo, player.zo)
			.length();

		if (player.onGround())
			ms.translate(0,
				Math.min(Math.abs(Mth.cos((AnimationTickHolder.getRenderTime() % 256) / 2.0f)) * -renderOffset.y, movement * 5),
				0);

		float interpolatedYaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());

		float scale = player.getScale();
		ms.scale(scale, scale, scale);

		try {
			PartialModel model = AllPartialModels.PACKAGES_TO_HIDE_AS.get(getCurrentBoxIndex(player));
			PackageRenderer.renderBox(player, interpolatedYaw, ms, bufferSource,
				packedLight, model);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		ms.popPose();
		return true;
	}

	private static Integer getCurrentBoxIndex(Player player) throws ExecutionException {
		return BOXES_PLAYERS_ARE_HIDING_AS.get(player.getUUID(),
			() -> player.level().random.nextInt(AllPartialModels.PACKAGES_TO_HIDE_AS.size()));
	}

}
