package com.simibubi.create.compat.jei.category;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	private final AnimatedSpout spout = new AnimatedSpout();

	public SpoutCategory(Info<FillingRecipe> info) {
		super(info);
	}

	public static void consumeRecipes(Consumer<RecipeHolder<FillingRecipe>> consumer, IIngredientManager ingredientManager) {
		Collection<IJeiFluidIngredient> fluidStacks = ingredientManager.getAllIngredients(FabricTypes.FLUID_STACK);
		for (ItemStack stack : ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
			if (PotionFluidHandler.isPotionItem(stack)) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
				ResourceLocation id = Create.asResource("potions");
				FillingRecipe recipe = new StandardProcessingRecipe.Builder<>(FillingRecipe::new, id)
						.withItemIngredients(bottle)
						.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
						.withSingleItemOutput(stack)
						.build();
				consumer.accept(new RecipeHolder<>(id, recipe));
				continue;
			}

			Storage<FluidVariant> capability = FluidStorage.ITEM.find(stack, new MutableContainerItemContext(stack));
			if (capability == null)
				continue;

			int numTanks = CreateTransferUtil.getSlotCount(capability);
			FluidStack existingFluid = numTanks == 1 ? Objects.requireNonNullElse(TransferUtil.getFirstFluid(capability), FluidStack.EMPTY) : FluidStack.EMPTY;

			for (IJeiFluidIngredient fluidIngredient : fluidStacks) {
				FluidStack fluidStack = new FluidStack(fluidIngredient.getFluidVariant(), fluidIngredient.getAmount());
				// Hoist the fluid equality check to avoid the work of copying the stack + populating capabilities
				// when most fluids will not match
				if (numTanks == 1 && (!existingFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(existingFluid, fluidStack)))
					continue;

				ItemStack copy = stack.copy();
				Storage<FluidVariant> fhi = FluidStorage.ITEM.find(stack, new MutableContainerItemContext(stack));
				if (fhi != null) {
					if (!GenericItemFilling.isFluidHandlerValid(copy, fhi))
						continue;
					FluidStack fluidCopy = fluidStack.copy();
					fluidCopy.setAmount(FluidConstants.BLOCK);
					CreateTransferUtil.insertFluid(fhi, fluidCopy, false);
					ItemStack container = copy;
					if (ItemHelper.sameItem(container, copy))
						continue;
					if (container.isEmpty())
						continue;

					Ingredient bucket = Ingredient.of(stack);
					ResourceLocation itemName = RegisteredObjectsHelper.getKeyOrThrow(stack.getItem());
					ResourceLocation fluidName = RegisteredObjectsHelper.getKeyOrThrow(fluidCopy.getFluid());
					ResourceLocation id = Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
							+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath());
					FillingRecipe recipe = new StandardProcessingRecipe.Builder<>(FillingRecipe::new, id)
							.withItemIngredients(bucket)
							.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
							.withSingleItemOutput(container)
							.build();
					consumer.accept(new RecipeHolder<>(id, recipe));
				}
			}
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FillingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		addFluidSlot(builder, 27, 32, recipe.getRequiredFluid());

		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(getResultItem(recipe));
	}

	@Override
	public void draw(FillingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
		spout.withFluids(recipe.getRequiredFluid()
			.getMatchingFluidStacks())
			.draw(graphics, getBackground().getWidth() / 2 - 13, 22);
	}

}
