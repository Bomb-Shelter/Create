package com.simibubi.create.foundation.mixin.accessor.fabric;

import net.minecraft.data.DataGenerator.PackGenerator;

import net.minecraft.data.PackOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackGenerator.class)
public interface DataGenerator$PackGeneratorAccessor {
	@Accessor
	PackOutput getOutput();
}
