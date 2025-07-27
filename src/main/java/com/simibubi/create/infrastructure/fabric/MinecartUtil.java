package com.simibubi.create.infrastructure.fabric;

import io.github.fabricators_of_create.porting_lib.blocks.util.MinecartAndRailUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public class MinecartUtil {
	public static BlockPos getCurrentRailPosition(AbstractMinecart minecart) {
		int x = Mth.floor(minecart.getX());
		int y = Mth.floor(minecart.getY());
		int z = Mth.floor(minecart.getZ());
		BlockPos pos = new BlockPos(x, y, z);
		if (minecart.level().getBlockState(pos.below()).is(BlockTags.RAILS))
			pos = pos.below();
		return pos;
	}

	public static double getSlopeAdjustment(AbstractMinecart minecart) {
		return MinecartAndRailUtil.getSlopeAdjustment();
	}
}
