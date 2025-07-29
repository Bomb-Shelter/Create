package com.simibubi.create.content.fluids;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.ICapabilityProvider;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.math.BlockFace;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public abstract class FlowSource {

	private static final ICapabilityProvider<Storage<FluidVariant>> EMPTY = null;

	BlockFace location;

	public FlowSource(BlockFace location) {
		this.location = location;
	}

	public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
		@Nullable ICapabilityProvider<Storage<FluidVariant>> tankCache = provideHandler();
		if (tankCache == null)
			return FluidStack.EMPTY;
		Storage<FluidVariant> tank = tankCache.getCapability();
		if (tank == null)
			return FluidStack.EMPTY;
		return CreateTransferUtil.extractFluidMatching(tank, extractionPredicate, 1, true);
	}

	// Layer III. PFIs need active attention to prevent them from disengaging early
	public void keepAlive() {}

	public abstract boolean isEndpoint();

	public void manageSource(Level world, BlockEntity networkBE) {
	}

	public void whileFlowPresent(Level world, boolean pulling) {}

	public @Nullable ICapabilityProvider<Storage<FluidVariant>> provideHandler() {
		return EMPTY;
	}

	public static class FluidHandler extends FlowSource {
		@Nullable
		ICapabilityProvider<Storage<FluidVariant>> fluidHandlerCache;

		public FluidHandler(BlockFace location) {
			super(location);
			fluidHandlerCache = EMPTY;
		}

		public void manageSource(Level level, BlockEntity networkBE) {
			if (fluidHandlerCache == null) {
				BlockEntity blockEntity = level.getBlockEntity(location.getConnectedPos());
				if (blockEntity != null) {
					if (level instanceof ServerLevel serverLevel) {
						fluidHandlerCache = ICapabilityProvider.of(BlockApiCache.create(
							FluidStorage.SIDED,
							serverLevel,
							blockEntity.getBlockPos()
						), location.getOppositeFace());
					} else if (level instanceof PonderLevel) {
						fluidHandlerCache = ICapabilityProvider.of(() -> FluidStorage.SIDED.find(
							level,
							blockEntity.getBlockPos(),
							location.getOppositeFace()
						));
					}
				}
			}
		}

		@Override
		@Nullable
		public ICapabilityProvider<Storage<FluidVariant>> provideHandler() {
			return fluidHandlerCache;
		}

		@Override
		public boolean isEndpoint() {
			return true;
		}
	}

	public static class OtherPipe extends FlowSource {
		WeakReference<FluidTransportBehaviour> cached;

		public OtherPipe(BlockFace location) {
			super(location);
		}

		@Override
		public void manageSource(Level world, BlockEntity networkBE) {
			if (cached != null && cached.get() != null && !cached.get().blockEntity.isRemoved())
				return;
			cached = null;
			FluidTransportBehaviour fluidTransportBehaviour =
				BlockEntityBehaviour.get(world, location.getConnectedPos(), FluidTransportBehaviour.TYPE);
			if (fluidTransportBehaviour != null)
				cached = new WeakReference<>(fluidTransportBehaviour);
		}

		@Override
		public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
			if (cached == null || cached.get() == null)
				return FluidStack.EMPTY;
			FluidTransportBehaviour behaviour = cached.get();
			FluidStack providedOutwardFluid = behaviour.getProvidedOutwardFluid(location.getOppositeFace());
			return extractionPredicate.test(providedOutwardFluid) ? providedOutwardFluid : FluidStack.EMPTY;
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

	public static class Blocked extends FlowSource {

		public Blocked(BlockFace location) {
			super(location);
		}

		@Override
		public boolean isEndpoint() {
			return false;
		}

	}

}
