package arghorror.network;

import arghorror.Arghorror;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SanityPacket(int sanity) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SanityPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Arghorror.MOD_ID, "sanity"));

    public static final StreamCodec<FriendlyByteBuf, SanityPacket> CODEC =
        StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.INT,
            SanityPacket::sanity,
            SanityPacket::new
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
