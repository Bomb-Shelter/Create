package com.simibubi.create.content.trains.entity;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record CarriageDataUpdatePacket(int entityId, CarriageSyncData data) implements ClientboundPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, CarriageDataUpdatePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, CarriageDataUpdatePacket::entityId,
		CarriageSyncData.STREAM_CODEC, CarriageDataUpdatePacket::data,
		CarriageDataUpdatePacket::new
	);

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(LocalPlayer player) {
		Entity entity = player.level().getEntity(entityId);
		if (entity instanceof CarriageContraptionEntity carriage) {
			carriage.onCarriageDataUpdate(this.data);
		} else {
			Create.LOGGER.error("Invalid CarriageDataUpdatePacket for non-carriage entity: " + entity);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CARRIAGE_DATA_UPDATE;
	}
}
