package com.simibubi.create;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;

public class AllAttachmentTypes {
	public static final AttachmentType<MinecartController> MINECART_CONTROLLER = AttachmentRegistry.create(ResourceLocation.fromNamespaceAndPath(Create.ID, "minecart_controller"), builder ->
		builder.persistent(MinecartController.CODEC)
			.initializer(() -> MinecartController.EMPTY)
	);

	@Internal
	public static void register() {
		//ATTACHMENT_TYPES.register(modEventBus);
	}
}
