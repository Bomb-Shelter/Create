package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record ContraptionDisassemblyPacket(int entityId, StructureTransform transform) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionDisassemblyPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionDisassemblyPacket::entityId,
			StructureTransform.STREAM_CODEC, ContraptionDisassemblyPacket::transform,
			ContraptionDisassemblyPacket::new
	);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		AbstractContraptionEntity.handleDisassemblyPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_DISASSEMBLE;
	}
}
