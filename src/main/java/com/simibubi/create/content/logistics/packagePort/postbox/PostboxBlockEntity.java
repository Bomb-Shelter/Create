package com.simibubi.create.content.logistics.packagePort.postbox;

import java.lang.ref.WeakReference;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.GlobalStation.GlobalPackagePort;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.nbt.NBTHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class PostboxBlockEntity extends PackagePortBlockEntity implements SidedStorageBlockEntity {

	public WeakReference<GlobalStation> trackedGlobalStation;

	public LerpedFloat flag;
	public boolean forceFlag;

	private boolean sendParticles;

	public PostboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		trackedGlobalStation = new WeakReference<>(null);
		flag = LerpedFloat.linear()
			.startWithValue(0);
	}

	public static void registerCapabilities() {
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
		return this.itemHandler;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide && !isVirtual()) {
			if (sendParticles)
				sendData();
			return;
		}

		float currentTarget = flag.getChaseTarget();
		if (currentTarget == 0 || flag.settled()) {
			int target = (inventory.isEmpty() && !forceFlag) ? 0 : 1;
			if (target != currentTarget) {
				flag.chase(target, 0.1f, Chaser.LINEAR);
				if (target == 1)
					AllSoundEvents.CONTRAPTION_ASSEMBLE.playAt(level, worldPosition, 1, 2, true);
			}
		}
		boolean settled = flag.getValue() > .15f;
		flag.tickChaser();
		if (currentTarget == 0 && settled != flag.getValue() > .15f)
			AllSoundEvents.CONTRAPTION_DISASSEMBLE.playAt(level, worldPosition, 0.75f, 1.5f, true);

		if (sendParticles) {
			sendParticles = false;
			BoneMealItem.addGrowthParticles(level, worldPosition, 40);
		}
	}

	@Override
	protected void onOpenChange(boolean open) {
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(PostboxBlock.OPEN, open));
		level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
			SoundSource.BLOCKS);
	}

	public void spawnParticles() {
		sendParticles = true;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		if (clientPacket && sendParticles)
			NBTHelper.putMarker(tag, "Particles");
		sendParticles = false;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		sendParticles = clientPacket && tag.contains("Particles");
	}

	@Override
	public void onChunkUnloaded() {
		if (level == null || level.isClientSide)
			return;
		GlobalStation station = trackedGlobalStation.get();
		if (station == null)
			return;
		if (!station.connectedPorts.containsKey(worldPosition))
			return;
		GlobalPackagePort globalPackagePort = station.connectedPorts.get(worldPosition);
		for (int i = 0; i < inventory.getSlotCount(); i++) {
			globalPackagePort.offlineBuffer.setStackInSlot(i, inventory.getStackInSlot(i));
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}

		globalPackagePort.primed = true;
		Create.RAILWAYS.markTracksDirty();
		super.onChunkUnloaded();
	}

}
