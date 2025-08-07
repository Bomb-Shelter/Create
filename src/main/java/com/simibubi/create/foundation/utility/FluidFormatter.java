package com.simibubi.create.foundation.utility;

import com.simibubi.create.infrastructure.fabric.transfer.CreateTransferUtil;

import net.createmod.catnip.data.Couple;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FluidFormatter {

	public static String asString(long amount, boolean shorten) {
		Couple<MutableComponent> couple = asComponents(amount, shorten);
		return couple.getFirst().getString() + " " + couple.getSecond().getString();
	}

	public static Couple<MutableComponent> asComponents(long amount, boolean shorten) {
		if (shorten && amount >= FluidConstants.BLOCK) {
			return Couple.create(
					Component.literal(String.format("%.1f" , amount / (double) FluidConstants.BLOCK)),
					CreateLang.translateDirect("generic.unit.buckets")
			);
		}

		return Couple.create(
				Component.literal(String.valueOf(CreateTransferUtil.dropletsToMb(amount))),
				CreateLang.translateDirect("generic.unit.millibuckets")
		);
	}

}
