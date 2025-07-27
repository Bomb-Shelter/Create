package com.simibubi.create.content.trains.entity;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents.EnteringSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Removes all Carriage entities in chunks that aren't ticking
 */
public class CarriageEntityHandler {

	public static void onEntityEnterSection(EnteringSection event) {
		if (!event.didChunkChange())
			return;
		Entity entity = event.getEntity();
		if (!(entity instanceof CarriageContraptionEntity cce))
			return;
		SectionPos newPos = event.getNewPos();
		Level level = entity.level();
		if (level.isClientSide)
			return;
		if (!isActiveChunk(level, newPos.center()))
			cce.leftTickingChunks = true;
	}

	public static void validateCarriageEntity(CarriageContraptionEntity entity) {
		if (!entity.isAlive())
			return;
		Level level = entity.level();
		if (level.isClientSide)
			return;
		if (!isActiveChunk(level, entity.blockPosition()))
			entity.leftTickingChunks = true;
	}

	public static boolean isActiveChunk(Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel)
			return serverLevel.isPositionEntityTicking(pos);
		return false;
	}

}
