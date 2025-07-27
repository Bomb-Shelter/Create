package com.simibubi.create;

import com.simibubi.create.content.schematics.SchematicProcessor;

import io.github.fabricators_of_create.porting_lib.registry.DeferredHolder;
import io.github.fabricators_of_create.porting_lib.registry.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllStructureProcessorTypes {
	private static final DeferredRegister<StructureProcessorType<?>> REGISTER = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Create.ID);

	public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<SchematicProcessor>> SCHEMATIC = REGISTER.register("schematic", () -> () -> SchematicProcessor.CODEC);

	@Internal
	public static void register() {
		REGISTER.register();
	}
}
