package com.simibubi.create.content.contraptions.actors.trainControls;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum ControlsStopControllingPacket implements ClientboundPacketPayload {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ControlsStopControllingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		ControlsHandler.stopControlling();
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTROLS_ABORT;
	}
}
