package com.simibubi.create.infrastructure.fabric;

import io.github.fabricators_of_create.porting_lib.blocks.BlockEvents.HarvestCheck;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomFrictionBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomRunningEffectsBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomSoundTypeBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.HarvestableBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.StickToBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.StickyBlock;
import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
import io.github.fabricators_of_create.porting_lib.item.extensions.CustomSupportsEnchantItem;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class CreateFabricUtil {
	public static boolean canStickTo(BlockState state, BlockState other) {
		if (state.getBlock() instanceof StickToBlock stickToBlock)
			return stickToBlock.canStickTo(state, other);

		if (state.getBlock() == Blocks.HONEY_BLOCK && other.getBlock() == Blocks.SLIME_BLOCK)
			return false;
		if (state.getBlock() == Blocks.SLIME_BLOCK && other.getBlock() == Blocks.HONEY_BLOCK)
			return false;
		return isStickyBlock(state) || isStickyBlock(other);
	}

	public static boolean isStickyBlock(BlockState state) {
		if (state.getBlock() instanceof StickyBlock stickyBlock)
			return stickyBlock.isStickyBlock(state);

		return state.getBlock() == Blocks.SLIME_BLOCK || state.getBlock() == Blocks.HONEY_BLOCK;
	}

	@Nullable
	public static DyeColor getColor(ItemStack stack) {
		if (stack.getItem() instanceof DyeItem dyeItem)
			return dyeItem.getDyeColor();

		for (DyeColor color : DyeColor.values()) {
			if (stack.is(color.getTag()))
				return color;
		}

		return null;
	}

	public static SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
		if (state.getBlock() instanceof CustomSoundTypeBlock soundTypeBlock)
			return soundTypeBlock.getSoundType(state, level, pos, entity);
		else
			return state.getSoundType();
	}

	public static boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
		if (state.getBlock() instanceof HarvestableBlock harvestableBlock)
			return harvestableBlock.canHarvestBlock(state, level, pos, player);

		var event = new HarvestCheck(player, state, level, pos, player.hasCorrectToolForDrops(state));
		event.sendEvent();
		return event.canHarvest();
	}

	public static float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
		if (state.getBlock() instanceof CustomFrictionBlock frictionBlock)
			return frictionBlock.getFriction(state, level, pos, entity);

		return state.getBlock().getFriction();
	}

	@Nullable
	public static BlockState getAxeStrippingState(BlockState originalState) {
		Block block = AxeItem.STRIPPABLES.get(originalState.getBlock());
		return block != null ? block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, originalState.getValue(RotatedPillarBlock.AXIS)) : null;
	}

	public static boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (state.getBlock() instanceof CustomRunningEffectsBlock runningEffectsBlock)
			return runningEffectsBlock.addRunningEffects(state, level, pos, entity);

		return false;
	}

	public static boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		if (stack.getItem() instanceof CustomSupportsEnchantItem supportsEnchantItem)
			return supportsEnchantItem.supportsEnchantment(stack, enchantment);

		return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
	}
}
