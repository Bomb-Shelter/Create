package com.simibubi.create.content.fluids.transfer;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;

import com.simibubi.create.infrastructure.fabric.transfer.ActuallyMutableContainerItemContext;
import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.impl.transfer.fluid.EmptyBucketStorage;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class GenericItemFilling {

	/**
	 * Checks if an ItemStack's IFluidHandlerItem is valid. Ideally, this check would
	 * not be necessary. Unfortunately, some mods that copy the functionality of the
	 * MilkBucketItem copy the FluidBucketWrapper capability that is patched in by
	 * Forge without looking into what it actually does. In all cases this is
	 * incorrect because having a non-bucket item turn into a bucket item does not
	 * make sense.
	 *
	 * <p>This check is only necessary for filling since a FluidBucketWrapper will be
	 * empty if it is initialized with a non-bucket item.
	 *
	 * @param stack The ItemStack.
	 * @param fluidHandler The IFluidHandlerItem instance retrieved from the ItemStack.
	 * @return If the IFluidHandlerItem is valid for the passed ItemStack.
	 */
	public static boolean isFluidHandlerValid(ItemStack stack, Storage<FluidVariant> fluidHandler) {
		// Not instanceof in case a correct subclass is made
		if (fluidHandler.getClass() == FullItemFluidStorage.class) {
			Item item = stack.getItem();
			// Forge does not patch the FluidBucketWrapper onto subclasses of BucketItem
			if (item.getClass() != BucketItem.class && !(item instanceof MilkBucketItem)) {
				return false;
			}
		}
		return true;
	}

	public static boolean canItemBeFilled(Level world, ItemStack stack) {
		if (stack.getItem() == Items.GLASS_BOTTLE)
			return true;
		if (stack.getItem() == Items.MILK_BUCKET)
			return false;

		Storage<FluidVariant> capability = FluidStorage.ITEM.find(stack, new MutableContainerItemContext(stack));
		if (capability == null)
			return false;
		if (!isFluidHandlerValid(stack, capability))
			return false;
		for (StorageView<FluidVariant> view : capability) {
			if (view.getAmount() < view.getCapacity())
				return true;
		}
		return false;
	}

	public static long getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(availableFluid))
			return FluidConstants.BOTTLE;
		if (stack.getItem() == Items.BUCKET && canFillBucketInternally(availableFluid))
			return FluidConstants.BUCKET;

		Storage<FluidVariant> capability = FluidStorage.ITEM.find(stack, new MutableContainerItemContext(stack));
		if (capability == null || capability instanceof EmptyBucketStorage)
			return -1;
		if (capability instanceof FullItemFluidStorage) {
			Item filledBucket = availableFluid.getFluid()
				.getBucket();
			if (filledBucket == null || filledBucket == Items.AIR)
				return -1;
			if (!((FullItemFluidStorage) capability).isResourceBlank())
				return -1;
			return FluidConstants.BUCKET;
		}

		long filled = CreateTransferUtil.simulateInsertFluid(capability, availableFluid);
		return filled == 0 ? -1 : filled;
	}

	private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
		Fluid fluid = availableFluid.getFluid();
		if (fluid.isSame(Fluids.WATER))
			return true;
		if (fluid.isSame(AllFluids.POTION.get()))
			return true;
		if (fluid.isSame(AllFluids.TEA.get()))
			return true;
		return false;
	}

	private static boolean canFillBucketInternally(FluidStack availableFluid) {
		return false;
	}

	public static ItemStack fillItem(Level world, long requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);
		availableFluid.shrink(requiredAmount);

		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
			ItemStack fillBottle;
			Fluid fluid = toFill.getFluid();
			if (FluidHelper.isWater(fluid))
				fillBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
			else if (fluid.isSame(AllFluids.TEA.get()))
				fillBottle = AllItems.BUILDERS_TEA.asStack();
			else
				fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
			stack.shrink(1);
			return fillBottle;
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		ActuallyMutableContainerItemContext context = new ActuallyMutableContainerItemContext(stack);
		Storage<FluidVariant> capability = FluidStorage.ITEM.find(stack, context);
		if (capability == null)
			return ItemStack.EMPTY;
		TransferUtil.insertFluid(capability, toFill);
		ItemStack container = context.getContainer()
			.copy();
		stack.shrink(1);
		return container;
	}

}
