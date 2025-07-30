package com.simibubi.create.content.kinetics.deployer;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import io.github.fabricators_of_create.porting_lib.core.event.CancellableEvent;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class DeployerRecipeSearchEvent extends BaseEvent implements CancellableEvent {
	public static final Event<DeployerRecipeSearchCallback> EVENT = EventFactory.createArrayBacked(DeployerRecipeSearchCallback.class, events -> event -> {
		for (DeployerRecipeSearchCallback callback : events) {
			callback.onDeployerRecipeSearch(event);
		}
	});

	private final DeployerBlockEntity blockEntity;
	private final RecipeWrapper inventory;
	@Nullable
	RecipeHolder<? extends Recipe<? extends RecipeInput>> recipe = null;
	private int maxPriority = 0;

	public DeployerRecipeSearchEvent(DeployerBlockEntity blockEntity, RecipeWrapper inventory) {
		this.blockEntity = blockEntity;
		this.inventory = inventory;
	}

	public DeployerBlockEntity getBlockEntity() {
		return blockEntity;
	}

	public RecipeWrapper getInventory() {
		return inventory;
	}

	// lazyness to not scan for recipes that aren't selected
	public boolean shouldAddRecipeWithPriority(int priority) {
		return !isCanceled() && priority > maxPriority;
	}

	@Nullable
	public RecipeHolder<? extends Recipe<? extends RecipeInput>> getRecipe() {
		if (isCanceled())
			return null;
		return recipe;
	}

	public void addRecipe(Supplier<Optional<? extends RecipeHolder<? extends Recipe<? extends RecipeInput>>>> recipeSupplier, int priority) {
		if (!shouldAddRecipeWithPriority(priority))
			return;
		recipeSupplier.get().ifPresent(newRecipe -> {
			this.recipe = newRecipe;
			maxPriority = priority;
		});
	}

	@Override
	public void sendEvent() {
		EVENT.invoker().onDeployerRecipeSearch(this);
	}

	@FunctionalInterface
	public interface DeployerRecipeSearchCallback {
		void onDeployerRecipeSearch(DeployerRecipeSearchEvent event);
	}
}
