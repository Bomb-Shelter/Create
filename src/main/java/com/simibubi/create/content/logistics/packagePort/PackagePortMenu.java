package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import com.simibubi.create.foundation.item.SmartInventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PackagePortMenu extends MenuBase<PackagePortBlockEntity, BlockPos> {

	public PackagePortMenu(MenuType<?> type, int id, Inventory inv, BlockPos extraData) {
		super(type, id, inv, extraData, true);
	}

	public PackagePortMenu(MenuType<?> type, int id, Inventory inv, PackagePortBlockEntity be) {
		super(type, id, inv, be);
		BlockEntityBehaviour.get(be, AnimatedContainerBehaviour.TYPE)
			.startOpen(player);
	}

	public static PackagePortMenu create(int id, Inventory inv, PackagePortBlockEntity be) {
		return new PackagePortMenu(AllMenuTypes.PACKAGE_PORT.get(), id, inv, be);
	}

	@Override
	protected PackagePortBlockEntity createOnClient(BlockPos readBlockPos) {
		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity blockEntity = world.getBlockEntity(readBlockPos);
		if (blockEntity instanceof PackagePortBlockEntity ppbe)
			return ppbe;
		return null;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int size = contentHolder.inventory.getSlotCount();
		boolean success = false;
		if (index < size) {
			success = !moveItemStackTo(stack, size, slots.size(), false);
		} else
			success = !moveItemStackTo(stack, 0, size, false);

		return success ? ItemStack.EMPTY : stack;
	}

	@Override
	protected void initAndReadInventory(PackagePortBlockEntity contentHolder) {}

	@Override
	protected void addSlots() {
		SmartInventory inventory = contentHolder.inventory;
		int x = 27;
		int y = 9;

		for (int row = 0; row < 2; row++)
			for (int col = 0; col < 9; col++)
				addSlot(new SlotItemHandler(inventory, row * 9 + col, x + col * 18, y + row * 18));

		addPlayerSlots(38, 108);
	}

	@Override
	protected void saveData(PackagePortBlockEntity contentHolder) {}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (!playerIn.level().isClientSide)
			BlockEntityBehaviour.get(contentHolder, AnimatedContainerBehaviour.TYPE)
				.stopOpen(playerIn);
	}

}
