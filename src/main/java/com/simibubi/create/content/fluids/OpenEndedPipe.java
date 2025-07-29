package com.simibubi.create.content.fluids;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import com.simibubi.create.infrastructure.fabric.transfer.FinalCommitSnapshot;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllFluids;
import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.content.fluids.pipes.VanillaFluidTargets;
import com.simibubi.create.foundation.ICapabilityProvider;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.mixin.accessor.FlowingFluidAccessor;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class OpenEndedPipe extends FlowSource {

	private Level world;
	private BlockPos pos;
	private AABB aoe;

	private OpenEndFluidHandler fluidHandler;
	private BlockPos outputPos;
	private boolean wasPulling;

	private final ICapabilityProvider<Storage<FluidVariant>> fluidHandlerProvider = ICapabilityProvider.of(() -> fluidHandler);

	public OpenEndedPipe(BlockFace face) {
		super(face);
		fluidHandler = new OpenEndFluidHandler();
		outputPos = face.getConnectedPos();
		pos = face.getPos();
		aoe = new AABB(outputPos).expandTowards(0, -1, 0);
		if (face.getFace() == Direction.DOWN)
			aoe = aoe.expandTowards(0, -1, 0);
	}

	public Level getWorld() {
		return world;
	}

	public BlockPos getPos() {
		return pos;
	}

	public BlockPos getOutputPos() {
		return outputPos;
	}

	public AABB getAOE() {
		return aoe;
	}

	@Override
	public void manageSource(Level world, BlockEntity networkBE) {
		this.world = world;
	}

	@Override
	@Nullable
	public ICapabilityProvider<Storage<FluidVariant>> provideHandler() {
		return fluidHandlerProvider;
	}

	@Override
	public boolean isEndpoint() {
		return true;
	}

	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		CompoundTag compound = new CompoundTag();
		fluidHandler.writeToNBT(registries, compound);
		compound.putBoolean("Pulling", wasPulling);
		compound.put("Location", location.serializeNBT());
		return compound;
	}

	public static OpenEndedPipe fromNBT(CompoundTag compound, HolderLookup.Provider registries, BlockPos blockEntityPos) {
		BlockFace fromNBT = BlockFace.fromNBT(compound.getCompound("Location"));
		OpenEndedPipe oep = new OpenEndedPipe(new BlockFace(blockEntityPos, fromNBT.getFace()));

		oep.fluidHandler.readFromNBT(registries, compound);
		oep.wasPulling = compound.getBoolean("Pulling");
		return oep;
	}

	private FluidStack removeFluidFromSpace(boolean simulate) {
		FluidStack empty = FluidStack.EMPTY;
		if (world == null)
			return empty;
		if (!world.isLoaded(outputPos))
			return empty;

		BlockState state = world.getBlockState(outputPos);
		FluidState fluidState = state.getFluidState();
		boolean waterlog = state.hasProperty(WATERLOGGED);

		FluidStack drainBlock = VanillaFluidTargets.drainBlock(world, outputPos, state, simulate);
		if (!drainBlock.isEmpty()) {
			if (!simulate && state.hasProperty(BlockStateProperties.LEVEL_HONEY)
				&& AllFluids.HONEY.is(drainBlock.getFluid()))
				AdvancementBehaviour.tryAward(world, pos, AllAdvancements.HONEY_DRAIN);
			return drainBlock;
		}

		if (!waterlog && !state.canBeReplaced())
			return empty;
		if (fluidState.isEmpty() || !fluidState.isSource())
			return empty;

		FluidStack stack = new FluidStack(fluidState.getType(), FluidConstants.BLOCK);

		if (simulate)
			return stack;

		if (FluidHelper.isWater(stack.getFluid()))
			AdvancementBehaviour.tryAward(world, pos, AllAdvancements.WATER_SUPPLY);

		if (waterlog) {
			world.setBlock(outputPos, state.setValue(WATERLOGGED, false), 3);
			world.scheduleTick(outputPos, Fluids.WATER, 1);
		} else {
			var newState = fluidState.createLegacyBlock()
				.setValue(LiquidBlock.LEVEL, 14);

			var newFluidState = newState.getFluidState();

			if (newFluidState.getType() instanceof FlowingFluidAccessor flowing) {
				var potentiallyFilled = flowing.create$getNewLiquid(world, outputPos, newState);

				// Check if we'd immediately become the same fluid again.
				if (potentiallyFilled.equals(fluidState)) {
					// If so, no need to update the block state.
					return stack;
				}
			}

			world.setBlock(outputPos, newState, 3);
		}

		return stack;
	}

	private boolean provideFluidToSpace(FluidStack fluid, boolean simulate) {
		if (world == null)
			return false;
		if (!world.isLoaded(outputPos))
			return false;

		BlockState state = world.getBlockState(outputPos);
		FluidState fluidState = state.getFluidState();
		boolean waterlog = state.hasProperty(WATERLOGGED);

		if (!waterlog && !state.canBeReplaced())
			return false;
		if (fluid.isEmpty())
			return false;
		if (!(fluid.getFluid() instanceof FlowingFluid))
			return false;
		if (!FluidHelper.hasBlockState(fluid.getFluid()))
			return true;

		if (!fluidState.isEmpty() && FluidHelper.convertToStill(fluidState.getType()) != fluid.getFluid()) {
			FluidReactions.handlePipeSpillCollision(world, outputPos, fluid.getFluid(), fluidState);
			return false;
		}

		if (fluidState.isSource())
			return false;
		if (waterlog && fluid.getFluid() != Fluids.WATER)
			return false;
		if (simulate)
			return true;

		if (!AllConfigs.server().fluids.pipesPlaceFluidSourceBlocks.get())
			return true;

		if (world.dimensionType()
			.ultraWarm() && FluidHelper.isTag(fluid, FluidTags.WATER)) {
			int i = outputPos.getX();
			int j = outputPos.getY();
			int k = outputPos.getZ();
			world.playSound(null, i, j, k, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
				2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			return true;
		}

		if (waterlog) {
			world.setBlock(outputPos, state.setValue(WATERLOGGED, true), 3);
			world.scheduleTick(outputPos, Fluids.WATER, 1);
			return true;
		}

		world.setBlock(outputPos, fluid.getFluid()
			.defaultFluidState()
			.createLegacyBlock(), 3);
		return true;
	}

	private class OpenEndFluidHandler extends FluidTank {

		public OpenEndFluidHandler() {
			super(FluidConstants.BUCKET);
		}

		@Override
		public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			// Never allow being filled when a source is attached
			if (world == null)
				return 0;
			if (!world.isLoaded(outputPos))
				return 0;
			if (resource.isBlank() || maxAmount <= 0)
				return 0;
			FluidStack stack = new FluidStack(resource, maxAmount);
			if (!provideFluidToSpace(stack, true))
				return 0;

			FluidStack containedFluidStack = getFluid();
			boolean hasBlockState = FluidHelper.hasBlockState(containedFluidStack.getFluid());

			if (!containedFluidStack.isEmpty() && !FluidStack.isSameFluidSameComponents(containedFluidStack, stack))
				setFluid(FluidStack.EMPTY);
			if (wasPulling)
				wasPulling = false;

			OpenPipeEffectHandler effectHandler = OpenPipeEffectHandler.REGISTRY.get(resource.getFluid());
			if (effectHandler != null && !hasBlockState)
				stack = FluidHelper.copyStackWithAmount(stack, 1);

			long fill = super.insert(resource, maxAmount, transaction);

			FluidStack finalStack = stack;
			new FinalCommitSnapshot(maxAmount, () -> {
				if (effectHandler != null && !finalStack.isEmpty()) {
					// resource should be copied before giving it to the handler.
					// if hasBlockState is false, it was already copied above.
					FluidStack exposed = hasBlockState ? finalStack.copy() : finalStack;
					effectHandler.apply(world, aoe, exposed);
				}

				if (getFluidAmount() == FluidConstants.BLOCK || !hasBlockState)
					if (provideFluidToSpace(containedFluidStack, false))
						setFluid(FluidStack.EMPTY);
			}).updateSnapshots(transaction);
			return fill;
		}

		@Override
		public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
			return drainInner(maxAmount, new FluidStack(extractedVariant, maxAmount), transaction);
		}

		private long drainInner(long amount, @Nullable FluidStack filter, TransactionContext transaction) {
			boolean filterPresent = filter != null;

			if (world == null)
				return 0;
			if (!world.isLoaded(outputPos))
				return 0;
			if (amount == 0)
				return 0;
			if (amount > FluidConstants.BLOCK) {
				amount = FluidConstants.BLOCK;
				if (filterPresent)
					filter = FluidHelper.copyStackWithAmount(filter, amount);
			}

			if (!wasPulling)
				wasPulling = true;

			long drainedFromInternal = filterPresent ? super.extract(filter.getVariant(), amount, transaction) : super.extract(super.iterator().next().getResource(), amount, transaction);
			if (drainedFromInternal > 0)
				return drainedFromInternal;

			FluidStack drainedFromWorld = removeFluidFromSpace(true);
			(new FinalCommitSnapshot(amount, () -> removeFluidFromSpace(false)))
				.updateSnapshots(transaction);
			if (drainedFromWorld.isEmpty())
				return 0;
			if (filterPresent && !FluidStack.isSameFluidSameComponents(drainedFromWorld, filter))
				return 0;

			long remainder = drainedFromWorld.getAmount() - amount;
			drainedFromWorld.setAmount(amount);

			if (remainder > 0) {
				if (!getFluid().isEmpty() && !FluidStack.isSameFluidSameComponents(getFluid(), drainedFromWorld))
					setFluid(FluidStack.EMPTY);
				super.insert(drainedFromWorld.getVariant(), remainder, transaction);
			}
			return drainedFromWorld.getAmount();
		}

		@Override
		public boolean isResourceBlank() {
			if (!super.isResourceBlank())
				return false;
			return removeFluidFromSpace(true).isEmpty();
		}

		@Override
		public FluidVariant getResource() {
			if (!super.isResourceBlank())
				return super.getResource();
			return removeFluidFromSpace(true).getVariant();
		}

		@Override
		public long getAmount() {
			if (super.getAmount() != 0)
				return super.getAmount();
			return removeFluidFromSpace(true).getAmount();
		}
	}
}
