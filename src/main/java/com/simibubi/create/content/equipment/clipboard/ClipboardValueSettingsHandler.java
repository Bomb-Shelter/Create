package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import io.github.fabricators_of_create.porting_lib.core.event.CancellableEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.EntityInteract;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.EntityInteractSpecific;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.LeftClickBlock;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.RightClickBlock;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.RightClickItem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ClipboardValueSettingsHandler {
	static {
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ClipboardValueSettingsHandler::drawCustomBlockSelection);

		RightClickBlock.EVENT.register(ClipboardValueSettingsHandler::rightClickToCopy);
		LeftClickBlock.EVENT.register(ClipboardValueSettingsHandler::leftClickToPaste);
	}

	@Environment(EnvType.CLIENT)
	public static boolean drawCustomBlockSelection(WorldRenderContext context, HitResult hitResult) {
		if (!(hitResult instanceof BlockHitResult target))
			return true;

		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = target.getBlockPos();
		BlockState blockstate = mc.level.getBlockState(pos);

		if (mc.player == null || mc.player.isSpectator())
			return true;
		if (!mc.level.getWorldBorder()
			.isWithinBounds(pos))
			return true;
		if (!AllBlocks.CLIPBOARD.isIn(mc.player.getMainHandItem()))
			return true;
		if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return true;
		if (!(smartBE instanceof ClipboardBlockEntity) && !smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc
				&& cc.writeToClipboard(mc.level.registryAccess(), new CompoundTag(), target.getDirection()))
			&& !(smartBE instanceof ClipboardCloneable))
			return true;

		VoxelShape shape = blockstate.getShape(mc.level, pos);
		if (shape.isEmpty())
			return true;

		VertexConsumer vb = context.consumers()
			.getBuffer(RenderType.lines());
		Vec3 camPos = context.camera()
			.getPosition();

		PoseStack ms = context.matrixStack();

		ms.pushPose();
		ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
		TrackBlockOutline.renderShape(shape, ms, vb, true);

		ms.popPose();

		return false;
	}

	@Environment(EnvType.CLIENT)
	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult target))
			return;
		if (!AllBlocks.CLIPBOARD.isIn(mc.player.getMainHandItem()))
			return;
		BlockPos pos = target.getBlockPos();
		if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return;

		if (smartBE instanceof ClipboardBlockEntity) {
			List<MutableComponent> tip = new ArrayList<>();
			tip.add(CreateLang.translateDirect("clipboard.actions"));
            tip.add(CreateLang.translateDirect("clipboard.copy_other_clipboard", Component.keybind("key.use")));
			CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
			return;
		}

		CompoundTag tagElement = mc.player.getMainHandItem().get(AllDataComponents.CLIPBOARD_COPIED_VALUES);

		boolean canCopy = smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc
				&& cc.writeToClipboard(mc.level.registryAccess(), new CompoundTag(), target.getDirection()))
			|| smartBE instanceof ClipboardCloneable ccbe
				&& ccbe.writeToClipboard(mc.level.registryAccess(), new CompoundTag(), target.getDirection());

		boolean canPaste = tagElement != null && (smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc && cc.readFromClipboard(mc.level.registryAccess(),
				tagElement.getCompound(cc.getClipboardKey()), mc.player, target.getDirection(), true))
			|| smartBE instanceof ClipboardCloneable ccbe && ccbe.readFromClipboard(mc.level.registryAccess(),
				tagElement.getCompound(ccbe.getClipboardKey()), mc.player, target.getDirection(), true));

		if (!canCopy && !canPaste)
			return;

		List<MutableComponent> tip = new ArrayList<>();
		tip.add(CreateLang.translateDirect("clipboard.actions"));
		if (canCopy)
            tip.add(CreateLang.translateDirect("clipboard.to_copy", Component.keybind("key.use")));
		if (canPaste)
            tip.add(CreateLang.translateDirect("clipboard.to_paste", Component.keybind("key.attack")));

		CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
	}

	//@SubscribeEvent
	public static void rightClickToCopy(RightClickBlock event) {
		interact(event, false);
	}

	//@SubscribeEvent
	public static void leftClickToPaste(LeftClickBlock event) {
		interact(event, true);
	}

	private static void interact(PlayerInteractEvent event, boolean paste) {
		ItemStack itemStack = event.getItemStack();
		if (!AllBlocks.CLIPBOARD.isIn(itemStack))
			return;

		BlockPos pos = event.getPos();
		Level world = event.getLevel();
		Player player = event.getEntity();
		if (player != null && player.isSpectator())
			return;
		if (player.isShiftKeyDown())
			return;
		if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return;

		if (smartBE instanceof ClipboardBlockEntity cbe) {
			if (event instanceof CancellableEvent cancellableEvent) {
				cancellableEvent.setCanceled(true);

				switch (event) {
					case EntityInteractSpecific e -> e.setCancellationResult(InteractionResult.SUCCESS);
					case EntityInteract e -> e.setCancellationResult(InteractionResult.SUCCESS);
					case RightClickBlock e -> e.setCancellationResult(InteractionResult.SUCCESS);
					case RightClickItem e -> e.setCancellationResult(InteractionResult.SUCCESS);
					default -> {}
				}
			}

			if (!world.isClientSide()) {
				List<List<ClipboardEntry>> listTo = ClipboardEntry.readAll(itemStack);
				List<List<ClipboardEntry>> listFrom = ClipboardEntry.readAll(cbe.dataContainer);
				List<ClipboardEntry> toAdd = new ArrayList<>();

				for (List<ClipboardEntry> page : listFrom) {
					Copy: for (ClipboardEntry entry : page) {
						String entryToAdd = entry.text.getString();
						for (List<ClipboardEntry> pageTo : listTo)
							for (ClipboardEntry existing : pageTo)
								if (entryToAdd.equals(existing.text.getString()))
									continue Copy;
						toAdd.add(new ClipboardEntry(entry.checked, entry.text));
					}
				}

				for (ClipboardEntry entry : toAdd) {
					List<ClipboardEntry> page = null;
					for (List<ClipboardEntry> freePage : listTo) {
						if (freePage.size() > 11)
							continue;
						page = freePage;
						break;
					}
					if (page == null) {
						page = new ArrayList<>();
						listTo.add(page);
					}
					page.add(entry);
					ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
				}

				ClipboardEntry.saveAll(listTo, itemStack);
			}

			player.displayClientMessage(CreateLang.translate("clipboard.copied_from_clipboard", world.getBlockState(pos)
				.getBlock()
				.getName()
				.withStyle(ChatFormatting.WHITE))
				.style(ChatFormatting.GREEN)
				.component(), true);
			return;
		}

		CompoundTag tag = itemStack.get(AllDataComponents.CLIPBOARD_COPIED_VALUES);
		if (paste && tag == null)
			return;
		if (!paste)
			tag = new CompoundTag();

		boolean anySuccess = false;
		boolean anyValid = false;
		for (BlockEntityBehaviour behaviour : smartBE.getAllBehaviours()) {
			if (!(behaviour instanceof ClipboardCloneable cc))
				continue;
			anyValid = true;
			String clipboardKey = cc.getClipboardKey();
			if (paste) {
				anySuccess |=
					cc.readFromClipboard(world.registryAccess(), tag.getCompound(clipboardKey), player, event.getFace(), world.isClientSide());
				continue;
			}
			CompoundTag compoundTag = new CompoundTag();
			boolean success = cc.writeToClipboard(world.registryAccess(), compoundTag, event.getFace());
			anySuccess |= success;
			if (success)
				tag.put(clipboardKey, compoundTag);
		}

		if (smartBE instanceof ClipboardCloneable ccbe) {
			anyValid = true;
			String clipboardKey = ccbe.getClipboardKey();
			if (paste) {
				anySuccess |= ccbe.readFromClipboard(world.registryAccess(), tag.getCompound(clipboardKey), player, event.getFace(),
					world.isClientSide());
			} else {
				CompoundTag compoundTag = new CompoundTag();
				boolean success = ccbe.writeToClipboard(world.registryAccess(), compoundTag, event.getFace());
				anySuccess |= success;
				if (success)
					tag.put(clipboardKey, compoundTag);
			}
		}

		if (!anyValid)
			return;

		((CancellableEvent) event).setCanceled(true);
		if (event instanceof RightClickBlock rightClickBlock)
			rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

		if (world.isClientSide())
			return;
		if (!anySuccess)
			return;

		player.displayClientMessage(CreateLang
			.translate(paste ? "clipboard.pasted_to" : "clipboard.copied_from", world.getBlockState(pos)
				.getBlock()
				.getName()
				.withStyle(ChatFormatting.WHITE))
			.style(ChatFormatting.GREEN)
			.component(), true);

		if (!paste) {
			ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
			itemStack.set(AllDataComponents.CLIPBOARD_COPIED_VALUES, tag);
		}
	}

}
