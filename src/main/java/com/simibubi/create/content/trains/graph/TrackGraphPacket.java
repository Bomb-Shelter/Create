package com.simibubi.create.content.trains.graph;

import java.util.UUID;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.player.LocalPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class TrackGraphPacket implements ClientboundPacketPayload {

	public UUID graphId;
	public int netId;
	public boolean packetDeletesGraph;

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		this.handle(CreateClient.RAILWAYS, CreateClient.RAILWAYS.getOrCreateGraph(graphId, netId));
	}

	protected abstract void handle(GlobalRailwayManager manager, TrackGraph graph);

}
