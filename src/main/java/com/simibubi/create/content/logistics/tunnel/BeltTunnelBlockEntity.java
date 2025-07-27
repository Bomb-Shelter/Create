package com.simibubi.create.content.logistics.tunnel;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlock.Shape;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

public class BeltTunnelBlockEntity extends SmartBlockEntity implements SidedStorageBlockEntity {

	public Map<Direction, LerpedFloat> flaps;
	public Set<Direction> sides;

	protected Storage<ItemVariant> cap = null;
	protected List<Pair<Direction, Boolean>> flapsToSend;

	public BeltTunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		flaps = new EnumMap<>(Direction.class);
		sides = new HashSet<>();
		flapsToSend = new LinkedList<>();
	}

	public static void registerCapabilities() {
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
		if (this.cap == null) {
			if (AllBlocks.BELT.has(this.level.getBlockState(this.worldPosition.below()))) {
				BlockEntity beBelow = this.level.getBlockEntity(this.worldPosition.below());
				if (beBelow != null) {
					Storage<ItemVariant> capBelow = ItemStorage.SIDED.find(beBelow.getLevel(), beBelow.getBlockPos().below(), beBelow.getBlockState(), beBelow, Direction.UP);
					if (capBelow != null) {
						this.cap = capBelow;
					}
				}
			}
		}
		return this.cap;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//invalidateCapabilities();
	}

	protected void writeFlapsAndSides(CompoundTag compound) {
		ListTag flapsNBT = new ListTag();
		for (Direction direction : flaps.keySet())
			flapsNBT.add(IntTag.valueOf(direction.get3DDataValue()));
		compound.put("Flaps", flapsNBT);

		ListTag sidesNBT = new ListTag();
		for (Direction direction : sides)
			sidesNBT.add(IntTag.valueOf(direction.get3DDataValue()));
		compound.put("Sides", sidesNBT);
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
		writeFlapsAndSides(tag);
		super.writeSafe(tag, registries);
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		writeFlapsAndSides(compound);
		super.write(compound, registries, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		Set<Direction> newFlaps = new HashSet<>(6);
		ListTag flapsNBT = compound.getList("Flaps", Tag.TAG_INT);
		for (Tag inbt : flapsNBT)
			if (inbt instanceof IntTag)
				newFlaps.add(Direction.from3DDataValue(((IntTag) inbt).getAsInt()));

		sides.clear();
		ListTag sidesNBT = compound.getList("Sides", Tag.TAG_INT);
		for (Tag inbt : sidesNBT)
			if (inbt instanceof IntTag)
				sides.add(Direction.from3DDataValue(((IntTag) inbt).getAsInt()));

		for (Direction d : Iterate.directions)
			if (!newFlaps.contains(d))
				flaps.remove(d);
			else if (!flaps.containsKey(d))
				flaps.put(d, createChasingFlap());

		// Backwards compat
		if (!compound.contains("Sides") && compound.contains("Flaps"))
			sides.addAll(flaps.keySet());
		super.read(compound, registries, clientPacket);
		if (clientPacket)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> VisualizationHelper.queueUpdate(this));
	}

	private LerpedFloat createChasingFlap() {
		return LerpedFloat.linear()
			.startWithValue(.25f)
			.chase(0, .05f, Chaser.EXP);
	}

	public void updateTunnelConnections() {
		flaps.clear();
		sides.clear();
		BlockState tunnelState = getBlockState();
		for (Direction direction : Iterate.horizontalDirections) {
			if (direction.getAxis() != tunnelState.getValue(BlockStateProperties.HORIZONTAL_AXIS)) {
				boolean positive =
					direction.getAxisDirection() == AxisDirection.POSITIVE ^ direction.getAxis() == Axis.Z;
				Shape shape = tunnelState.getValue(BeltTunnelBlock.SHAPE);
				if (BeltTunnelBlock.isStraight(tunnelState))
					continue;
				if (positive && shape == Shape.T_LEFT)
					continue;
				if (!positive && shape == Shape.T_RIGHT)
					continue;
			}

			sides.add(direction);

			// Flap might be occluded
			if (level == null)
				continue;
			BlockState nextState = level.getBlockState(worldPosition.relative(direction));
			if (nextState.getBlock() instanceof BeltTunnelBlock)
				continue;
			if (nextState.getBlock() instanceof BeltFunnelBlock)
				if (nextState.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
					&& nextState.getValue(BeltFunnelBlock.HORIZONTAL_FACING) == direction.getOpposite())
					continue;

			flaps.put(direction, createChasingFlap());
		}
		sendData();
	}

	public void flap(Direction side, boolean inward) {
		if (level.isClientSide) {
			if (flaps.containsKey(side))
				flaps.get(side)
					.setValue(inward ? -1 : 1);
			return;
		}

		flapsToSend.add(Pair.of(side, inward));
	}

	@Override
	public void initialize() {
		super.initialize();
		updateTunnelConnections();
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide) {
			if (!flapsToSend.isEmpty())
				sendFlaps();
			return;
		}
		flaps.forEach((d, value) -> value.tickChaser());
	}

	private void sendFlaps() {
		if (level instanceof ServerLevel serverLevel)
			CatnipServices.NETWORK.sendToClientsTrackingChunk(serverLevel, new ChunkPos(worldPosition), new TunnelFlapPacket(this, flapsToSend));

		flapsToSend.clear();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}
