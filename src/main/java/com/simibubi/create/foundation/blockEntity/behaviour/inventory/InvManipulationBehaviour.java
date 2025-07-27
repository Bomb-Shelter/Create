package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.function.Predicate;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InvManipulationBehaviour extends CapManipulationBehaviourBase<Storage<ItemVariant>, InvManipulationBehaviour> {

	// Extra types available for multibehaviour
	public static final BehaviourType<InvManipulationBehaviour>

	TYPE = new BehaviourType<>(), EXTRACT = new BehaviourType<>(), INSERT = new BehaviourType<>();

	private BehaviourType<InvManipulationBehaviour> behaviourType;

	public static InvManipulationBehaviour forExtraction(SmartBlockEntity be, InterfaceProvider target) {
		return new InvManipulationBehaviour(EXTRACT, be, target);
	}

	public static InvManipulationBehaviour forInsertion(SmartBlockEntity be, InterfaceProvider target) {
		return new InvManipulationBehaviour(INSERT, be, target);
	}

	public InvManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
		this(TYPE, be, target);
	}

	private InvManipulationBehaviour(BehaviourType<InvManipulationBehaviour> type, SmartBlockEntity be,
		InterfaceProvider target) {
		super(be, target);
		behaviourType = type;
	}

	@Nullable
	public IdentifiedInventory getIdentifiedInventory() {
		Storage<ItemVariant> inventory = this.getInventory();
		if (inventory == null)
			return null;

		InventoryIdentifier identifier = InventoryIdentifier.get(this.getWorld(), this.getTarget().getOpposite());
		return new IdentifiedInventory(identifier, inventory);
	}

	@Override
	protected BlockApiLookup<Storage<ItemVariant>, Direction> capability() {
		return ItemStorage.SIDED;
	}

	public ItemStack extract() {
		return extract(getModeFromFilter(), getAmountFromFilter());
	}

	public ItemStack extract(ExtractionCountMode mode, int amount) {
		return extract(mode, amount, Predicates.alwaysTrue());
	}

	public ItemStack extract(ExtractionCountMode mode, int amount, Predicate<ItemStack> filter) {
		boolean shouldSimulate = simulateNext;
		simulateNext = false;

		if (getWorld().isClientSide)
			return ItemStack.EMPTY;
		Storage<ItemVariant> inventory = targetCapability;
		if (inventory == null)
			return ItemStack.EMPTY;

		Predicate<ItemStack> test = getFilterTest(filter);
		ItemStack simulatedItems = ItemHelper.extract(inventory, test, mode, amount, true);
		if (shouldSimulate || simulatedItems.isEmpty())
			return simulatedItems;
		return ItemHelper.extract(inventory, test, mode, amount, false);
	}

	public ItemStack insert(ItemStack stack) {
		boolean shouldSimulate = simulateNext;
		simulateNext = false;
		Storage<ItemVariant> inventory = targetCapability;
		if (inventory == null)
			return stack;
		return CreateTransferUtil.insertItemStacked(inventory, stack, shouldSimulate);
	}

	protected Predicate<ItemStack> getFilterTest(Predicate<ItemStack> customFilter) {
		Predicate<ItemStack> test = customFilter;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

}
