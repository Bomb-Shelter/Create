package com.simibubi.create.foundation.mixin.fabric.ponder;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;

import com.mojang.blaze3d.platform.InputConstants.Key;

import net.createmod.ponder.enums.PonderKeybinds;
import net.minecraft.client.KeyMapping;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
	@Shadow
	@Final
	private static Map<String, KeyMapping> ALL;
	private static final Map<InputConstants.Key, KeyMapping> PONDER_MAP = Maps.newHashMap();

	@Inject(method = "set", at = @At("HEAD"), cancellable = true)
	private static void fixSetPonderKeybind(InputConstants.Key key, boolean held, CallbackInfo ci) {
		KeyMapping keyMapping = PONDER_MAP.get(key);
		if (keyMapping != null) {
			keyMapping.setDown(held);
		}
	}

	@Inject(method = "click", at = @At("HEAD"), cancellable = true)
	private static void fixClickPonderKeybind(Key key, CallbackInfo ci) {
		KeyMapping keyMapping = PONDER_MAP.get(key);
		if (keyMapping != null) {
			++keyMapping.clickCount;
		}
	}

	@Inject(method = "resetMapping", at = @At("HEAD"))
	private static void resetPonderMappings(CallbackInfo ci) {
		PONDER_MAP.clear();

		for(KeyMapping keyMapping : ALL.values()) {
			PONDER_MAP.put(keyMapping.key, keyMapping);
		}
	}

	@ModifyReturnValue(method = "isDown", at = @At("RETURN"))
	private boolean modifyIsDown(boolean original) {
		// TODO this shouldn't be hardcoded but I don't want to reimplement forge's keybind system right now
		if ((Object) this == Minecraft.getInstance().options.keyUp) {
			return original && Minecraft.getInstance().screen == null;
		}
		return original;
	}

	@WrapOperation(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
	private Object dontAddPonderBecauseConflicts(Map<Key, KeyMapping> instance, Object k, Object v, Operation<KeyMapping> original) {
		if (v == PonderKeybinds.PONDER.getKeybind())
			return PONDER_MAP.put((Key) k, (KeyMapping) v);
		return original.call(instance, k, v);
	}
}
