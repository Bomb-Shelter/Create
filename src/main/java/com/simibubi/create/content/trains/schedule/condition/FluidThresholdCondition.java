package com.simibubi.create.content.trains.schedule.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.createmod.catnip.lang.Lang;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class FluidThresholdCondition extends CargoThresholdCondition {

	private FilterItemStack compareStack = FilterItemStack.empty();

	@Override
	protected Component getUnit() {
		return Component.literal("b");
	}

	@Override
	protected ItemStack getIcon() {
		return compareStack.item();
	}

	@Override
	protected boolean test(Level level, Train train, CompoundTag context) {
		Ops operator = getOperator();
		int target = getThreshold();

		long foundFluid = 0;
		for (Carriage carriage : train.carriages) {
			Storage<FluidVariant> fluids = carriage.storage.getFluids();
			for (StorageView<FluidVariant> view : fluids) {
				FluidStack fluidInTank = new FluidStack(view);
				if (!compareStack.test(level, fluidInTank))
					continue;
				foundFluid += fluidInTank.getAmount();
			}
		}

		requestStatusToUpdate(foundFluid / FluidConstants.BLOCK, context);
		return operator.test(foundFluid, target * FluidConstants.BLOCK);
	}

	@Override
	protected void writeAdditional(HolderLookup.Provider registries, CompoundTag tag) {
		super.writeAdditional(registries, tag);
		tag.put("Bucket", compareStack.serializeNBT(registries));
	}

	@Override
	protected void readAdditional(HolderLookup.Provider registries, CompoundTag tag) {
		super.readAdditional(registries, tag);
		if (tag.contains("Bucket"))
			compareStack = FilterItemStack.of(registries, tag.getCompound("Bucket"));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		return super.tickCompletion(level, train, context);
	}

	@Environment(EnvType.CLIENT)
	private FluidStack loadFluid() {
		return compareStack.fluid(Minecraft.getInstance().level);
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			CreateLang.translateDirect("schedule.condition.threshold.train_holds",
				CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(getOperator().name()))),
			CreateLang.translateDirect("schedule.condition.threshold.x_units_of_item", getThreshold(),
				CreateLang.translateDirect("schedule.condition.threshold.buckets"),
				compareStack.isEmpty() ? CreateLang.translateDirect("schedule.condition.threshold.anything")
					: compareStack.isFilterItem()
						? CreateLang.translateDirect("schedule.condition.threshold.matching_content")
						: loadFluid().getHoverName())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		compareStack = FilterItemStack.of(stack);
	}

	@Override
	public ItemStack getItem(int slot) {
		return compareStack.item();
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("fluid_threshold");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		super.initConfigurationWidgets(builder);
		builder.addSelectionScrollInput(71, 50, (i, l) -> {
			i.forOptions(ImmutableList.of(CreateLang.translateDirect("schedule.condition.threshold.buckets")))
				.titled(null);
		}, "Measure");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		long lastDisplaySnapshot = getLastDisplaySnapshot(tag);
		if (lastDisplaySnapshot == -1)
            return Component.empty();
		int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
		return CreateLang.translateDirect("schedule.condition.threshold.status", CreateTransferUtil.dropletsToMb(lastDisplaySnapshot),
			Math.max(0, getThreshold() + offset), CreateLang.translateDirect("schedule.condition.threshold.buckets"));
	}

}
