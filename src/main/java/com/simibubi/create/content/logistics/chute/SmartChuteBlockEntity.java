package com.simibubi.create.content.logistics.chute;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class SmartChuteBlockEntity extends ChuteBlockEntity implements SidedStorageBlockEntity {

	FilteringBehaviour filtering;

	public SmartChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities() {
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
		return this.itemHandler;
	}

	@Override
	protected boolean canAcceptItem(ItemStack stack) {
		return super.canAcceptItem(stack) && canActivate() && filtering.test(stack);
	}

	@Override
	protected int getExtractionAmount() {
		return filtering.isCountVisible() && !filtering.anyAmount() ? filtering.getAmount() : 64;
	}

	@Override
	protected ExtractionCountMode getExtractionMode() {
		return filtering.isCountVisible() && !filtering.anyAmount() && !filtering.upTo ? ExtractionCountMode.EXACTLY
			: ExtractionCountMode.UPTO;
	}

	@Override
	protected boolean canActivate() {
		BlockState blockState = getBlockState();
		return blockState.hasProperty(SmartChuteBlock.POWERED) && !blockState.getValue(SmartChuteBlock.POWERED);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering =
			new FilteringBehaviour(this, new SmartChuteFilterSlotPositioning()).showCountWhen(this::isExtracting)
				.withCallback($ -> invVersionTracker.reset()));
		super.addBehaviours(behaviours);
	}

	private boolean isExtracting() {
		boolean up = getItemMotion() < 0;
		BlockPos chutePos = worldPosition.relative(up ? Direction.UP : Direction.DOWN);
		BlockState blockState = level.getBlockState(chutePos);
		return !AbstractChuteBlock.isChute(blockState) && !blockState.canBeReplaced();
	}

}
