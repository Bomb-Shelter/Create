package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
	@Definition(id = "entity", local = @Local(type = Entity.class, ordinal = 0))
	@Definition(id = "entity2", local = @Local(type = Entity.class, ordinal = 2))
	@Definition(id = "getRootVehicle", method = "Lnet/minecraft/world/entity/Entity;getRootVehicle()Lnet/minecraft/world/entity/Entity;")
	@Expression("entity2.getRootVehicle() == entity.getRootVehicle()")
	@WrapOperation(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static boolean create$interactWithEntitiesOnContraptions(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) || ((Entity) left).getRootVehicle() instanceof AbstractContraptionEntity;
	}
}
