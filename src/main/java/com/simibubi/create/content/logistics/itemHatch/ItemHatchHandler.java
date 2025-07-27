package com.simibubi.create.content.logistics.itemHatch;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;

import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.RightClickBlock;
import net.fabricmc.fabric.api.util.TriState;


public class ItemHatchHandler {
	public static void init() {
		RightClickBlock.EVENT.register(ItemHatchHandler::useOnItemHatchIgnoresSneak);
	}

	//@SubscribeEvent(priority = EventPriority.LOW)
	public static void useOnItemHatchIgnoresSneak(RightClickBlock event) {
		if (event.getUseItem() == TriState.DEFAULT && AllBlocks.ITEM_HATCH.has(event.getLevel()
			.getBlockState(event.getPos())))
			event.setUseBlock(TriState.TRUE);
	}

}
