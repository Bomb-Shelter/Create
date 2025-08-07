package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.function.Predicate;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import com.google.common.base.Predicates;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

public class TankManipulationBehaviour extends CapManipulationBehaviourBase<Storage<FluidVariant>, TankManipulationBehaviour> {

	public static final BehaviourType<TankManipulationBehaviour> OBSERVE = new BehaviourType<>();
	private BehaviourType<TankManipulationBehaviour> behaviourType;

	public TankManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
		this(OBSERVE, be, target);
	}

	private TankManipulationBehaviour(BehaviourType<TankManipulationBehaviour> type, SmartBlockEntity be,
		InterfaceProvider target) {
		super(be, target);
		behaviourType = type;
	}

	public FluidStack extractAny() {
		if (!hasInventory())
			return FluidStack.EMPTY;
		Storage<FluidVariant> inventory = getInventory();
		Predicate<FluidStack> filterTest = getFilterTest(Predicates.alwaysTrue());
		for (StorageView<FluidVariant> view : inventory) {
			if (view.isResourceBlank() || view.getAmount() <= 0)
				continue;
			if (!filterTest.test(new FluidStack(view)))
				continue;
			try (Transaction transaction = CreateTransferUtil.getTransaction()) {
				long drained = view.extract(view.getResource(), view.getAmount(), transaction);

				if (!simulateNext)
					transaction.commit();

				if (drained > 0)
					return new FluidStack(view.getResource(), drained);
			}
		}

		return FluidStack.EMPTY;
	}

	protected Predicate<FluidStack> getFilterTest(Predicate<FluidStack> customFilter) {
		Predicate<FluidStack> test = customFilter;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	protected BlockApiLookup<Storage<FluidVariant>, Direction> capability() {
		return FluidStorage.SIDED;
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

}
