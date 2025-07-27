package com.simibubi.create.foundation.block;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.BlockHelper;

import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.RightClickBlock;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ItemUseOverrides {

	private static final Set<ResourceLocation> OVERRIDES = new HashSet<>();

	public static void addBlock(Block block) {
		OVERRIDES.add(RegisteredObjectsHelper.getKeyOrThrow(block));
	}

	static {
		RightClickBlock.EVENT.register(ItemUseOverrides::onBlockActivated);
	}

	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		if (AllItems.WRENCH.isIn(event.getItemStack()))
			return;

		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		Direction face = event.getFace();
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();

		BlockState state = level.getBlockState(pos);
		ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(state.getBlock());

		if (!OVERRIDES.contains(id))
			return;

		BlockHitResult blockTrace =
				new BlockHitResult(VecHelper.getCenterOf(pos), face, pos, true);
		InteractionResult result = BlockHelper.invokeUse(state, level, player, hand, blockTrace);

		if (!result.consumesAction())
			return;

		event.setCanceled(true);
		event.setCancellationResult(result);
	}
}
