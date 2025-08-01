package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.item.ItemStack;

public class ToolboxSlot extends SlotItemHandler {

	private ToolboxMenu toolboxMenu;
	private boolean isVisible;

	public ToolboxSlot(ToolboxMenu menu, SlottedStorage<ItemVariant> itemHandler, int index, int xPosition, int yPosition, boolean isVisible) {
		super(itemHandler, index, xPosition, yPosition);
		this.toolboxMenu = menu;
		this.isVisible = isVisible;
	}

	@Override
	public boolean isActive() {
		return !toolboxMenu.renderPass && super.isActive() && isVisible;
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		ItemStack maxAdd = stack.copy();
		int maxInput = stack.getMaxStackSize();
		maxAdd.setCount(maxInput);

		SlottedStorage<ItemVariant> handler = this.getItemHandler();
		SingleSlotStorage<ItemVariant> currentStack = handler.getSlot(this.port_lib$getSlotIndex());
		ItemStack remainder = CreateTransferUtil.insertItem(currentStack, maxAdd, true);
		long current = currentStack.getAmount();
		int added = maxInput - remainder.getCount();
		return (int) (current + added);
	}

}
