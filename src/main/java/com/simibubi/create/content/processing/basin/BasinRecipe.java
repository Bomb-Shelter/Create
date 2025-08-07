package com.simibubi.create.content.processing.basin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Iterators;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.DummyCraftingContainer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.createmod.catnip.data.Iterate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class BasinRecipe extends StandardProcessingRecipe<RecipeInput> {

	public static boolean match(BasinBlockEntity basin, Recipe<?> recipe) {
		FilteringBehaviour filter = basin.getFilter();
		if (filter == null)
			return false;

		boolean filterTest = filter.test(recipe.getResultItem(basin.getLevel()
			.registryAccess()));
		if (recipe instanceof BasinRecipe basinRecipe) {
			if (basinRecipe.getRollableResults()
				.isEmpty()
				&& !basinRecipe.getFluidResults()
				.isEmpty())
				filterTest = filter.test(basinRecipe.getFluidResults()
					.get(0));
		}

		if (!filterTest)
			return false;

		return apply(basin, recipe, true);
	}

	public static boolean apply(BasinBlockEntity basin, Recipe<?> recipe) {
		return apply(basin, recipe, false);
	}

	private static boolean apply(BasinBlockEntity basin, Recipe<?> recipe, boolean test) {
		boolean isBasinRecipe = recipe instanceof BasinRecipe;
		Storage<ItemVariant> itemStorage = TransferUtil.getItemStorage(basin.getLevel(), basin.getBlockPos(), basin, null);
		Storage<FluidVariant> fluidStorage = TransferUtil.getFluidStorage(basin.getLevel(), basin.getBlockPos(), basin, null);
		StorageView<ItemVariant>[] availableItems = Iterators.toArray(itemStorage.iterator(), StorageView.class);
		StorageView<FluidVariant>[] availableFluids = Iterators.toArray(fluidStorage.iterator(), StorageView.class);

		if (availableItems == null || availableFluids == null)
			return false;

		HeatLevel heat = BasinBlockEntity.getHeatLevelOf(basin.getLevel()
			.getBlockState(basin.getBlockPos()
				.below(1)));
		if (isBasinRecipe && !((BasinRecipe) recipe).getRequiredHeat()
			.testBlazeBurner(heat))
			return false;

		List<ItemStack> recipeOutputItems = new ArrayList<>();
		List<FluidStack> recipeOutputFluids = new ArrayList<>();

		List<Ingredient> ingredients = new LinkedList<>(recipe.getIngredients());
		List<FluidIngredient> fluidIngredients =
			isBasinRecipe ? ((BasinRecipe) recipe).getFluidIngredients() : Collections.emptyList();

		for (boolean simulate : Iterate.trueAndFalse) {

			if (!simulate && test)
				return true;

			long[] extractedItemsFromSlot = new long[availableItems.length];
			long[] extractedFluidsFromTank = new long[availableFluids.length];

			Ingredients:
			for (Ingredient ingredient : ingredients) {
				for (int slot = 0; slot < availableItems.length; slot++) {
					if (simulate && availableItems[slot]
						.getAmount() <= extractedItemsFromSlot[slot])
						continue;
					ItemStack extracted = CreateTransferUtil.extractItem(availableItems[slot], 1, true);
					if (!ingredient.test(extracted))
						continue;
					if (!simulate)
						CreateTransferUtil.extractItem(availableItems[slot], 1, false);
					extractedItemsFromSlot[slot]++;
					continue Ingredients;
				}

				// something wasn't found
				return false;
			}

			boolean fluidsAffected = false;
			FluidIngredients:
			for (FluidIngredient fluidIngredient : fluidIngredients) {
				long amountRequired = fluidIngredient.getRequiredAmount();

				for (int tank = 0; tank < availableFluids.length; tank++) {
					StorageView<FluidVariant> view = availableFluids[tank];
					FluidStack fluidStack = new FluidStack(view);
					if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
						continue;
					if (!fluidIngredient.test(fluidStack))
						continue;
					long drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
					if (!simulate) {
						try (Transaction tx = CreateTransferUtil.getTransaction()) {
							view.extract(view.getResource(), drainedAmount, tx);
							tx.commit();
						}
						fluidsAffected = true;
					}
					amountRequired -= drainedAmount;
					if (amountRequired != 0)
						continue;
					extractedFluidsFromTank[tank] += drainedAmount;
					continue FluidIngredients;
				}

				// something wasn't found
				return false;
			}

			if (fluidsAffected) {
				basin.getBehaviour(SmartFluidTankBehaviour.INPUT)
					.forEach(TankSegment::onFluidStackChanged);
				basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
					.forEach(TankSegment::onFluidStackChanged);
			}

			if (simulate) {
				CraftingInput remainderInput = new DummyCraftingContainer(itemStorage, extractedItemsFromSlot)
					.asCraftInput();

				if (recipe instanceof BasinRecipe basinRecipe) {
					recipeOutputItems.addAll(basinRecipe.rollResults());

					for (FluidStack fluidStack : basinRecipe.getFluidResults())
						if (!fluidStack.isEmpty())
							recipeOutputFluids.add(fluidStack);
					for (ItemStack stack : basinRecipe.getRemainingItems(remainderInput))
						if (!stack.isEmpty())
							recipeOutputItems.add(stack);

				} else {
					recipeOutputItems.add(recipe.getResultItem(basin.getLevel()
						.registryAccess()));

					if (recipe instanceof CraftingRecipe craftingRecipe) {
						for (ItemStack stack : craftingRecipe.getRemainingItems(remainderInput))
							if (!stack.isEmpty())
								recipeOutputItems.add(stack);
					}
				}
			}

			if (!basin.acceptOutputs(recipeOutputItems, recipeOutputFluids, simulate))
				return false;
		}

		return true;
	}

	public static RecipeHolder<BasinRecipe> convertShapeless(RecipeHolder<?> recipe) {
		BasinRecipe basinRecipe =
			new Builder<>(BasinRecipe::new, recipe.id()).withItemIngredients(recipe.value().getIngredients())
				.withSingleItemOutput(recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess()))
				.build();
		return new RecipeHolder<>(recipe.id(), basinRecipe);
	}

	protected BasinRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
		super(type, params);
	}

	public BasinRecipe(ProcessingRecipeParams params) {
		this(AllRecipeTypes.BASIN, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 9;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 2;
	}

	@Override
	protected boolean canRequireHeat() {
		return true;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}

	@Override
	public boolean matches(RecipeInput input, @Nonnull Level worldIn) {
		return false;
	}

}
