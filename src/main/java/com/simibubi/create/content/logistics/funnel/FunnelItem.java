package com.simibubi.create.content.logistics.funnel;

import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.RightClickBlock;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FunnelItem extends BlockItem {

	public FunnelItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	static {
		RightClickBlock.EVENT.register(FunnelItem::funnelItemAlwaysPlacesWhenUsed);
	}

	public static void funnelItemAlwaysPlacesWhenUsed(PlayerInteractEvent.RightClickBlock event) {
		if (event.getItemStack().getItem() instanceof FunnelItem)
			event.setUseBlock(TriState.FALSE);
	}

	@Override
	protected BlockState getPlacementState(BlockPlaceContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = super.getPlacementState(ctx);
		if (state == null)
			return state;
		if (!(state.getBlock() instanceof FunnelBlock))
			return state;
		if (state.getValue(FunnelBlock.FACING)
			.getAxis()
			.isVertical())
			return state;

		Direction direction = state.getValue(FunnelBlock.FACING);
		FunnelBlock block = (FunnelBlock) getBlock();
		Block beltFunnelBlock = block.getEquivalentBeltFunnel(world, pos, state)
			.getBlock();
		BlockState equivalentBeltFunnel = beltFunnelBlock.getStateForPlacement(ctx)
			.setValue(BeltFunnelBlock.HORIZONTAL_FACING, direction);
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, world, pos))
			return equivalentBeltFunnel;

		return state;
	}

}
