package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.Create;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CreateLang extends Lang {

	/**
	 * legacy-ish. Use CreateLang.translate and other builder methods where possible
	 *

	 */
	public static MutableComponent translateDirect(String key, Object... args) {
        Object[] args1 = LangBuilder.resolveBuilders(args);
        return Component.translatable(Create.ID + "." + key, args1);
    }

	public static List<Component> translatedOptions(String prefix, String... keys) {
		List<Component> result = new ArrayList<>(keys.length);
		for (String key : keys)
			result.add(translate((prefix != null ? prefix + "." : "") + key).component());
		return result;
	}

	//

	public static LangBuilder builder() {
		return new LangBuilder(Create.ID);
	}

	public static LangBuilder blockName(BlockState state) {
		return builder().add(state.getBlock()
				.getName());
	}

	public static LangBuilder itemName(ItemStack stack) {
		return builder().add(stack.getHoverName()
				.copy());
	}

	public static LangBuilder fluidName(FluidStack stack) {
		return builder().add(stack.getHoverName()
				.copy());
	}

	public static LangBuilder fluidName(FluidVariant variant) {
		if (variant.getFluid().getFluidType() == null)
			return builder().text(variant.getRegistryEntry().unwrapKey().orElseThrow().location().toString());

		return builder().add(variant.getFluid().getFluidType().getDescription()
			.copy());
	}

	public static LangBuilder number(double d) {
		return builder().text(LangNumberFormat.format(d));
	}

	public static LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}

	public static LangBuilder text(String text) {
		return builder().text(text);
	}

	@Deprecated // Use while implementing and replace all references with Lang.translate
	public static LangBuilder temporaryText(String text) {
		return builder().text(text);
	}

}
