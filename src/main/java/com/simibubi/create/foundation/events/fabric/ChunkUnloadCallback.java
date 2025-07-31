package com.simibubi.create.foundation.events.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

@FunctionalInterface
public interface ChunkUnloadCallback {
	Event<ChunkUnloadCallback> EVENT = EventFactory.createArrayBacked(ChunkUnloadCallback.class, callbacks -> (level, chunk) -> {
		for (ChunkUnloadCallback callback : callbacks) {
			callback.onChunkUnload(level, chunk);
		}
	});

	void onChunkUnload(Level level, ChunkAccess chunk);
}
