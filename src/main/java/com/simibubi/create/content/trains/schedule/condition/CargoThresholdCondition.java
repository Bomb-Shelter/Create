package com.simibubi.create.content.trains.schedule.condition;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.Lang;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class CargoThresholdCondition extends LazyTickedScheduleCondition {
	public static enum Ops {
		GREATER(">"), LESS("<"), EQUAL("=");

		public String formatted;

		private Ops(String formatted) {
			this.formatted = formatted;
		}

		public boolean test(long current, long target) {
			return switch (this) {
			case GREATER -> current > target;
			case EQUAL -> current == target;
			case LESS -> current < target;
			default -> throw new IllegalArgumentException("Unexpected value: " + this);
			};
		}

		public static List<? extends Component> translatedOptions() {
			return Arrays.stream(values())
				.map(op -> CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(op.name())))
				.toList();
		}
	}

	public CargoThresholdCondition() {
		super(20);
		data.putString("Threshold", "10");
	}

	@Override
	public boolean lazyTickCompletion(Level level, Train train, CompoundTag context) {
		int lastChecked = context.contains("LastChecked") ? context.getInt("LastChecked") : -1;
		int status = 0;
		for (Carriage carriage : train.carriages)
			status += carriage.storage.getVersion();
		if (status == lastChecked)
			return false;
		context.putLong("LastChecked", status);
		return test(level, train, context);
	}

	protected void requestStatusToUpdate(long amount, CompoundTag context) {
		context.putLong("CurrentDisplay", amount);
		super.requestStatusToUpdate(context);
	};

	protected long getLastDisplaySnapshot(CompoundTag context) {
		if (!context.contains("CurrentDisplay"))
			return -1;
		if (context.contains("CurrentDisplay", Tag.TAG_INT))
			return (long) ((context.getInt("CurrentDisplay") / 1000.0) * FluidConstants.BLOCK);

		return context.getLong("CurrentDisplay");
	}

	protected abstract boolean test(Level level, Train train, CompoundTag context);

	protected abstract Component getUnit();

	protected abstract ItemStack getIcon();

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(getIcon(), Component.literal(getOperator().formatted + " " + getThreshold()).append(getUnit()));
	}

	@Override
	public int slotsTargeted() {
		return 1;
	}

	public Ops getOperator() {
		return enumData("Operator", Ops.class);
	}

	public int getThreshold() {
		try {
			return Integer.valueOf(textData("Threshold"));
		} catch (NumberFormatException e) {
			data.putString("Threshold", "0");
		}
		return 0;
	}

	public int getMeasure() {
		return intData("Measure");
	}

	@Override
	public List<Component> getSecondLineTooltip(int slot) {
		return ImmutableList.of(CreateLang.translateDirect("schedule.condition.threshold.place_item"),
			CreateLang.translateDirect("schedule.condition.threshold.place_item_2")
				.withStyle(ChatFormatting.GRAY),
			CreateLang.translateDirect("schedule.condition.threshold.place_item_3")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		builder.addSelectionScrollInput(0, 24, (i, l) -> {
			i.forOptions(Ops.translatedOptions())
				.titled(CreateLang.translateDirect("schedule.condition.threshold.train_holds", ""))
				.format(state -> {
                    return Component.literal(" " + Ops.values()[state].formatted);
                });
		}, "Operator");
		builder.addIntegerTextInput(29, 41, (e, t) -> {
		}, "Threshold");
	}

}
