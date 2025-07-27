package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record ContraptionRelocationPacket(int entityId) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionRelocationPacket> STREAM_CODEC = ByteBufCodecs.INT.map(
			ContraptionRelocationPacket::new, ContraptionRelocationPacket::entityId
	);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		OrientedContraptionEntity.handleRelocationPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_RELOCATION;
	}
}
