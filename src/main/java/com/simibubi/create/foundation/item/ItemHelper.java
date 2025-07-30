package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.mixin.accessor.ItemStackHandlerAccessor;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemHelper {

	public static boolean sameItem(ItemStack stack, ItemStack otherStack) {
		return !otherStack.isEmpty() && stack.is(otherStack.getItem());
	}

	public static Predicate<ItemStack> sameItemPredicate(ItemStack stack) {
		return s -> sameItem(stack, s);
	}

	public static void dropContents(Level world, BlockPos pos, Storage<ItemVariant> inv) {
		for (StorageView<ItemVariant> view : inv) {
			int maxStackSize = view.getResource().getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, view.getResource().getItem().getDefaultMaxStackSize());
			long total = view.getAmount();

			while (total > 0) {
				Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), view.getResource().toStack((int) Math.min(maxStackSize, total)));
				total -= maxStackSize;
			}
		}
	}

	public static List<ItemStack> multipliedOutput(ItemStack in, ItemStack out) {
		List<ItemStack> stacks = new ArrayList<>();
		ItemStack result = out.copy();
		result.setCount(in.getCount() * out.getCount());

		while (result.getCount() > result.getOrDefault(DataComponents.MAX_STACK_SIZE, 64)) {
			stacks.add(result.split(result.getOrDefault(DataComponents.MAX_STACK_SIZE, 64)));
		}

		stacks.add(result);
		return stacks;
	}

	public static void addToList(ItemStack stack, List<ItemStack> stacks) {
		for (ItemStack s : stacks) {
			if (!ItemStack.isSameItemSameComponents(stack, s))
				continue;
			int transferred = Math.min(s.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) - s.getCount(), stack.getCount());
			s.grow(transferred);
			stack.shrink(transferred);
		}
		if (stack.getCount() > 0)
			stacks.add(stack);
	}

	public static <T> boolean isSameInventory(SlottedStorage<T> h1, SlottedStorage<T> h2) {
		if (h1 == null || h2 == null)
			return false;

		if (h1.getSlotCount() != h2.getSlotCount())
			return false;
		for (int slot = 0; slot < h1.getSlotCount(); slot++) {
			if (h1.getSlot(slot) != h2.getSlot(slot))
				return false;
		}
		return true;
	}

	public static <T extends IBE<? extends BlockEntity>> int calcRedstoneFromBlockEntity(T ibe, Level level, BlockPos pos) {
		return ibe.getBlockEntityOptional(level, pos)
			.map(be -> TransferUtil.getItemStorage(level, pos, be, null))
			.map(ItemHelper::calcRedstoneFromInventory)
			.orElse(0);
	}

	public static int calcRedstoneFromInventory(@Nullable Storage<ItemVariant> inv) {
		if (inv == null)
			return 0;
		int i = 0;
		float f = 0.0F;
		int totalSlots = 0;
		for (StorageView<ItemVariant> view : inv) {
			long slotLimit = view.getCapacity();
			if (slotLimit == 0) {
				continue;
			}
			totalSlots++;
			if (!view.isResourceBlank() && view.getAmount() > 0) {
				f += (float) view.getAmount() / (float) Math.min(slotLimit, view.getResource().getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
				++i;
			}
		}

		if (totalSlots == 0)
			return 0;

		f = f / totalSlots;
		return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
	}

	public static List<Pair<Ingredient, MutableInt>> condenseIngredients(NonNullList<Ingredient> recipeIngredients) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = new ArrayList<>();
		Ingredients:
		for (Ingredient igd : recipeIngredients) {
			for (Pair<Ingredient, MutableInt> pair : actualIngredients) {
				ItemStack[] stacks1 = pair.getFirst()
					.getItems();
				ItemStack[] stacks2 = igd.getItems();
				if (stacks1.length != stacks2.length)
					continue;
				for (int i = 0; i <= stacks1.length; i++) {
					if (i == stacks1.length) {
						pair.getSecond()
							.increment();
						continue Ingredients;
					}
					if (!ItemStack.matches(stacks1[i], stacks2[i]))
						break;
				}
			}
			actualIngredients.add(Pair.of(igd, new MutableInt(1)));
		}
		return actualIngredients;
	}

	public static boolean matchIngredients(Ingredient i1, Ingredient i2) {
		if (i1 == i2)
			return true;
		ItemStack[] stacks1 = i1.getItems();
		ItemStack[] stacks2 = i2.getItems();
		if (stacks1 == stacks2)
			return true;
		if (stacks1.length == stacks2.length) {
			for (int i = 0; i < stacks1.length; i++)
				if (!ItemStack.isSameItem(stacks1[i], stacks2[i]))
					return false;
			return true;
		}
		return false;
	}

	public static boolean matchAllIngredients(NonNullList<Ingredient> ingredients) {
		if (ingredients.size() <= 1)
			return true;
		Ingredient firstIngredient = ingredients.get(0);
		for (int i = 1; i < ingredients.size(); i++)
			if (!matchIngredients(firstIngredient, ingredients.get(i)))
				return false;
		return true;
	}

	public static enum ExtractionCountMode {
		EXACTLY, UPTO
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.UPTO, 64, simulate);
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, int exactAmount, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.EXACTLY, exactAmount, simulate);
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, ExtractionCountMode mode, int amount,
									boolean simulate) {
		ItemStack extracting = ItemStack.EMPTY;
		boolean amountRequired = mode == ExtractionCountMode.EXACTLY;
		boolean checkHasEnoughItems = amountRequired;
		boolean hasEnoughItems = !checkHasEnoughItems;
		boolean potentialOtherMatch = false;
		int maxExtractionCount = amount;

		Extraction:
		do {
			extracting = ItemStack.EMPTY;

			for (StorageView<ItemVariant> view : inv.nonEmptyViews()) {
				int amountToExtractFromThisSlot =
					Math.min(maxExtractionCount - extracting.getCount(), view.getResource().getComponentMap()
						.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
				long extracted = StorageUtil.simulateExtract(view, view.getResource(), amountToExtractFromThisSlot, null);

				if (extracted <= 0)
					continue;
				ItemStack stack = view.getResource().toStack((int) Math.min(extracted, 64));
				if (!test.test(stack))
					continue;
				if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting)) {
					potentialOtherMatch = true;
					continue;
				}

				if (extracting.isEmpty())
					extracting = stack.copy();
				else
					extracting.grow(stack.getCount());

				if (!simulate && hasEnoughItems)
					try (Transaction trans = TransferUtil.getTransaction()) {
						view.extract(view.getResource(), extracted, trans);
						trans.commit();
					}

				if (extracting.getCount() >= maxExtractionCount) {
					if (checkHasEnoughItems) {
						hasEnoughItems = true;
						checkHasEnoughItems = false;
						continue Extraction;
					} else {
						break Extraction;
					}
				}
			}

			if (!extracting.isEmpty() && !hasEnoughItems && potentialOtherMatch) {
				ItemStack blackListed = extracting.copy();
				test = test.and(i -> !ItemStack.isSameItemSameComponents(i, blackListed));
				continue;
			}

			if (checkHasEnoughItems)
				checkHasEnoughItems = false;
			else
				break Extraction;

		} while (true);

		if (amountRequired && extracting.getCount() < amount)
			return ItemStack.EMPTY;

		return extracting;
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test,
									Function<ItemStack, Integer> amountFunction, boolean simulate) {
		ItemStack extracting = ItemStack.EMPTY;
		int maxExtractionCount = 64;

		for (StorageView<ItemVariant> view : inv) {
			if (extracting.isEmpty()) {
				ItemStack stackInSlot = view.getResource().toStack((int) Math.min(view.getAmount(), 64));
				if (stackInSlot.isEmpty() || !test.test(stackInSlot))
					continue;
				int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
				if (maxExtractionCountForItem == 0)
					continue;
				maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
			}

			long extracted = StorageUtil.simulateExtract(view, view.getResource(), maxExtractionCount - extracting.getCount(), null);
			ItemStack stack = view.getResource().toStack((int) Math.min(extracted, 64));

			if (!test.test(stack))
				continue;
			if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting))
				continue;

			if (extracting.isEmpty())
				extracting = stack.copy();
			else
				extracting.grow(stack.getCount());

			if (!simulate)
				try (Transaction trans = TransferUtil.getTransaction()) {
					view.extract(view.getResource(), extracted, trans);
					trans.commit();
				}
			if (extracting.getCount() >= maxExtractionCount)
				break;
		}

		return extracting;
	}

	public static boolean canItemStackAmountsStack(ItemStack a, ItemStack b) {
		return ItemStack.isSameItemSameComponents(a, b) && a.getCount() + b.getCount() <= a.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
	}

	public static ItemStack findFirstMatch(SlottedStackStorage inv, Predicate<ItemStack> test) {
		int slot = findFirstMatchingSlotIndex(inv, test);
		if (slot == -1)
			return ItemStack.EMPTY;
		else
			return inv.getStackInSlot(slot);
	}

	public static int findFirstMatchingSlotIndex(SlottedStackStorage inv, Predicate<ItemStack> test) {
		for (int slot = 0; slot < inv.getSlotCount(); slot++) {
			ItemStack toTest = inv.getStackInSlot(slot);
			if (test.test(toTest))
				return slot;
		}
		return -1;
	}

	public static ItemStack fromItemEntity(Entity entityIn) {
		if (!entityIn.isAlive())
			return ItemStack.EMPTY;
		if (entityIn instanceof PackageEntity packageEntity) {
			return packageEntity.getBox();
		}
		return entityIn instanceof ItemEntity itemEntity ? itemEntity.getItem() : ItemStack.EMPTY;
	}

	public static void fillItemStackHandler(ItemContainerContents contents, ItemStackHandler inv) {
		List<ItemStack> itemStacks = contents.stream().toList();

		for (int i = 0; i < itemStacks.size(); i++) {
			inv.setStackInSlot(i, itemStacks.get(i));
		}
	}

	public static ItemContainerContents containerContentsFromHandler(ItemStackHandler handler) {
		return ItemContainerContents.fromItems(handler.getSlots().stream().map(e -> e.getResource().toStack((int) Math.min(e.getAmount(), 64))).toList());
	}

	public static ItemStack limitCountToMaxStackSize(ItemStack stack, boolean simulate) {
		int count = stack.getCount();
		int max = stack.getMaxStackSize();
		if (count <= max)
			return ItemStack.EMPTY;
		ItemStack remainder = stack.copyWithCount(count - max);
		if (!simulate)
			stack.setCount(max);
		return remainder;
	}

	public static void copyContents(SlottedStackStorage from, SlottedStackStorage to) {
		if (from.getSlots() != to.getSlots()) {
			throw new IllegalArgumentException("Slot count mismatch");
		}

		for (int i = 0; i < from.getSlotCount(); i++) {
			to.setStackInSlot(i, from.getStackInSlot(i).copy());
		}
	}

	public static List<ItemStack> getNonEmptyStacks(ItemStackHandler handler) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < handler.getSlotCount(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}
		return stacks;
	}
}
