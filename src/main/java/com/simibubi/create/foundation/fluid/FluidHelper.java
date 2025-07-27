package com.simibubi.create.foundation.fluid;

import javax.annotation.Nullable;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.fluids.BaseFlowingFluid;
import io.github.fabricators_of_create.porting_lib.fluids.sound.SoundActions;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.createmod.catnip.data.Pair;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class FluidHelper {

	public static enum FluidExchange {
		ITEM_TO_TANK, TANK_TO_ITEM;
	}

	public static boolean isWater(Fluid fluid) {
		return convertToStill(fluid) == Fluids.WATER;
	}

	public static boolean isWater(FluidVariant fluidVariant) {
		return isWater(fluidVariant.getFluid());
	}

	public static boolean isLava(Fluid fluid) {
		return convertToStill(fluid) == Fluids.LAVA;
	}

	public static boolean isSame(FluidStack fluidStack, FluidStack fluidStack2) {
		return fluidStack.getFluid() == fluidStack2.getFluid();
	}

	public static boolean isSame(FluidStack fluidStack, Fluid fluid) {
		return fluidStack.getFluid() == fluid;
	}

	@SuppressWarnings("deprecation")
	public static boolean isTag(Fluid fluid, TagKey<Fluid> tag) {
		return fluid.is(tag);
	}

	public static boolean isTag(FluidState fluid, TagKey<Fluid> tag) {
		return fluid.is(tag);
	}

	public static boolean isTag(FluidStack fluid, TagKey<Fluid> tag) {
		return isTag(fluid.getFluid(), tag);
	}

	public static SoundEvent getFillSound(FluidStack fluid) {
		SoundEvent soundevent = fluid.getFluid()
			.getFluidType()
			.getSound(fluid, SoundActions.BUCKET_FILL);
		if (soundevent == null)
			soundevent =
				FluidHelper.isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
		return soundevent;
	}

	public static SoundEvent getEmptySound(FluidStack fluid) {
		SoundEvent soundevent = fluid.getFluid()
			.getFluidType()
			.getSound(fluid, SoundActions.BUCKET_EMPTY);
		if (soundevent == null)
			soundevent =
				FluidHelper.isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		return soundevent;
	}

	public static boolean hasBlockState(Fluid fluid) {
		BlockState blockState = fluid.defaultFluidState()
			.createLegacyBlock();
		return blockState != null && blockState != Blocks.AIR.defaultBlockState();
	}

	public static FluidStack copyStackWithAmount(FluidStack fs, int amount) {
		if (amount <= 0)
			return FluidStack.EMPTY;
		if (fs.isEmpty())
			return FluidStack.EMPTY;
		FluidStack copy = fs.copy();
		copy.setAmount(amount);
		return copy;
	}

	public static FluidStack copyStackWithAmount(FluidStack fs, long amount) {
		if (amount <= 0)
			return FluidStack.EMPTY;
		if (fs.isEmpty())
			return FluidStack.EMPTY;
		FluidStack copy = fs.copy();
		copy.setAmount(amount);
		return copy;
	}

	public static Fluid convertToFlowing(Fluid fluid) {
		if (fluid == Fluids.WATER)
			return Fluids.FLOWING_WATER;
		if (fluid == Fluids.LAVA)
			return Fluids.FLOWING_LAVA;
		if (fluid instanceof BaseFlowingFluid)
			return ((BaseFlowingFluid) fluid).getFlowing();
		return fluid;
	}

	public static Fluid convertToStill(Fluid fluid) {
		if (fluid == Fluids.FLOWING_WATER)
			return Fluids.WATER;
		if (fluid == Fluids.FLOWING_LAVA)
			return Fluids.LAVA;
		if (fluid instanceof BaseFlowingFluid)
			return ((BaseFlowingFluid) fluid).getSource();
		return fluid;
	}

	public static boolean tryEmptyItemIntoBE(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartBlockEntity be) {
		if (!GenericItemEmptying.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemStack> emptyingResult = GenericItemEmptying.emptyItem(worldIn, heldItem, true);
		Storage<FluidVariant> capability = TransferUtil.getFluidStorage(worldIn, be.getBlockPos(), be, null);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (capability == null || fluidStack.getAmount() != CreateTransferUtil.simulateInsertFluid(capability, fluidStack))
			return false;
		if (worldIn.isClientSide)
			return true;

		ItemStack copyOfHeld = heldItem.copy();
		emptyingResult = GenericItemEmptying.emptyItem(worldIn, copyOfHeld, false);
		TransferUtil.insertFluid(capability, fluidStack);

		if (!player.isCreative() && !(be instanceof CreativeFluidTankBlockEntity)) {
			if (copyOfHeld.isEmpty())
				player.setItemInHand(handIn, emptyingResult.getSecond());
			else {
				player.setItemInHand(handIn, copyOfHeld);
				player.getInventory()
					.placeItemBackInInventory(emptyingResult.getSecond());
			}
		}
		return true;
	}

	public static boolean tryFillItemFromBE(Level world, Player player, InteractionHand handIn, ItemStack heldItem,
		SmartBlockEntity be) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		Storage<FluidVariant> capability = TransferUtil.getFluidStorage(world, be.getBlockPos(), be, null);

		if (capability == null)
			return false;

		for (StorageView<FluidVariant> fluid : capability) {
			if (fluid.isResourceBlank() || fluid.getAmount() <= 0)
				continue;
			long requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, new FluidStack(fluid));
			if (requiredAmountForItem == -1)
				continue;
			if (requiredAmountForItem > fluid.getAmount())
				continue;

			if (world.isClientSide)
				return true;

			if (player.isCreative() || be instanceof CreativeFluidTankBlockEntity)
				heldItem = heldItem.copy();
			ItemStack out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, new FluidStack(fluid));

			FluidStack copy = new FluidStack(fluid);
			copy.setAmount(requiredAmountForItem);
			CreateTransferUtil.extractFluid(fluid, copy, false);

			if (!player.isCreative())
				player.getInventory()
					.placeItemBackInInventory(out);
			be.notifyUpdate();
			return true;
		}

		return false;
	}

	@Nullable
	public static FluidExchange exchange(Storage<FluidVariant> fluidTank, Storage<FluidVariant> fluidItem, FluidExchange preferred,
		int maxAmount) {
		return exchange(fluidTank, fluidItem, preferred, true, maxAmount);
	}

	@Nullable
	public static FluidExchange exchangeAll(Storage<FluidVariant> fluidTank, Storage<FluidVariant> fluidItem,
		FluidExchange preferred) {
		return exchange(fluidTank, fluidItem, preferred, false, Integer.MAX_VALUE);
	}

	@Nullable
	private static FluidExchange exchange(Storage<FluidVariant> fluidTank, Storage<FluidVariant> fluidItem, FluidExchange preferred,
		boolean singleOp, int maxTransferAmountPerTank) {

		// Locks in the transfer direction of this operation
		FluidExchange lockedExchange = null;

		for (StorageView<FluidVariant> fluidInTank : fluidTank) {
			for (StorageView<FluidVariant> fluidInItem : fluidItem) {
				long tankCapacity = fluidInTank.getCapacity() - fluidInTank.getAmount();
				boolean tankEmpty = fluidInTank.isResourceBlank() || fluidInTank.getAmount() <= 0;

				long itemCapacity = fluidInItem.getCapacity() - fluidInItem.getAmount();
				boolean itemEmpty = fluidInItem.isResourceBlank() || fluidInItem.getAmount() <= 0;

				boolean undecided = lockedExchange == null;
				boolean canMoveToTank = (undecided || lockedExchange == FluidExchange.ITEM_TO_TANK) && tankCapacity > 0;
				boolean canMoveToItem = (undecided || lockedExchange == FluidExchange.TANK_TO_ITEM) && itemCapacity > 0;

				// Incompatible Liquids
				if (!tankEmpty && !itemEmpty && !FluidStack.isSameFluidSameComponents(new FluidStack(fluidInItem), new FluidStack(fluidInTank)))
					continue;

				// Transfer liquid to tank
				if (((tankEmpty || itemCapacity <= 0) && canMoveToTank)
					|| undecided && preferred == FluidExchange.ITEM_TO_TANK) {

					long amount = CreateTransferUtil.insertFluid(fluidTank,
						CreateTransferUtil.extractAnyFluid(fluidItem, Math.min(maxTransferAmountPerTank, tankCapacity), false),
						false);
					if (amount > 0) {
						lockedExchange = FluidExchange.ITEM_TO_TANK;
						if (singleOp)
							return lockedExchange;
						continue;
					}
				}

				// Transfer liquid from tank
				if (((itemEmpty || tankCapacity <= 0) && canMoveToItem)
					|| undecided && preferred == FluidExchange.TANK_TO_ITEM) {

					long amount = CreateTransferUtil.insertFluid(fluidItem,
						CreateTransferUtil.extractAnyFluid(fluidTank, Math.min(maxTransferAmountPerTank, itemCapacity), false),
						false);
					if (amount > 0) {
						lockedExchange = FluidExchange.TANK_TO_ITEM;
						if (singleOp)
							return lockedExchange;
						continue;
					}

				}

			}
		}

		return null;
	}

}
