package arghorror.network;

import arghorror.Arghorror;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Kept as stub to avoid breaking client references — SanityHud is now a no-op.
public record SanityPacket(int sanity) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SanityPacket> TYPE =
        new CustomPacketPayload.Type<>(Arghorror.id("sanity"));
    public static final StreamCodec<FriendlyByteBuf, SanityPacket> CODEC =
        StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.INT,
            SanityPacket::sanity,
            SanityPacket::new
        );
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
