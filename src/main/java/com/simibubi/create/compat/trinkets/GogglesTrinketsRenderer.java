package com.simibubi.create.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class GogglesTrinketsRenderer implements TrinketRenderer {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Create.asResource("goggles"), "goggles");

	@Override
	public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> model, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {
		if (AllItems.GOGGLES.isIn(stack) &&
			model instanceof PlayerModel playerModel &&
			entity instanceof AbstractClientPlayer player) {

			// Translate and rotate with our head
			matrixStack.pushPose();
			TrinketRenderer.followBodyRotations(entity, playerModel);
			TrinketRenderer.translateToFace(matrixStack, playerModel, player, headYaw, headPitch);

			// Translate and scale to our head
			matrixStack.translate(0, 0, 0.3);
			matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
			matrixStack.scale(0.625f, 0.625f, 0.625f);

			if (headOccupied(entity)) {
				matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
				matrixStack.translate(0, -0.25, 0);
			}

			// Render
			Minecraft mc = Minecraft.getInstance();
			mc.getItemRenderer()
				.renderStatic(stack, ItemDisplayContext.HEAD, light, OverlayTexture.NO_OVERLAY, matrixStack,
					renderTypeBuffer, mc.level, 0);
			matrixStack.popPose();
		}
	}

	public static boolean headOccupied(LivingEntity entity) {
		if (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
			return true;
		return TrinketsApi.getTrinketComponent(entity)
			.filter(component -> {							 // guaranteed  // may be null
				TrinketInventory inv = component.getInventory().get("head").get("goggles");
				return inv != null && !inv.isEmpty();
			}).isPresent();
	}
}
