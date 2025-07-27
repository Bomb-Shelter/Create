package com.simibubi.create.content.logistics.crate;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class CreativeCrateBlockEntity extends CrateBlockEntity implements SidedStorageBlockEntity {
	FilteringBehaviour filtering;
	BottomlessItemHandler inv;

	public CreativeCrateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inv = new BottomlessItemHandler(filtering::getFilter);
	}

	public static void registerCapabilities() {
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
		return inv;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = createFilter());
		filtering.setLabel(CreateLang.translateDirect("logistics.creative_crate.supply"));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//if (inv != null)
			//invalidateCapabilities();
	}

	public FilteringBehaviour createFilter() {
		return new FilteringBehaviour(this, new ValueBoxTransform() {

			@Override
			public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
				TransformStack.of(ms)
					.rotateXDegrees(90);
			}

			@Override
			public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
				return new Vec3(0.5, 13.5 / 16d, 0.5);
			}

			public float getScale() {
				return super.getScale();
			};

		});
	}

}
