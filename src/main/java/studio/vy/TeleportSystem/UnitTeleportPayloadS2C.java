package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import studio.vy.Blossom;

public record UnitTeleportPayloadS2C(boolean flag) implements CustomPayload {
    public static final Id<UnitTeleportPayloadS2C> ID = CustomPayload.id("unit_teleport_s2c");
    public static final PacketCodec<PacketByteBuf, UnitTeleportPayloadS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN,
            UnitTeleportPayloadS2C::flag,
            UnitTeleportPayloadS2C::new
    );


    public static void send(ServerPlayerEntity player, boolean flag) {
        ServerPlayNetworking.send(player, new UnitTeleportPayloadS2C(flag));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<UnitTeleportPayloadS2C> {
        @Override
        public void receive(UnitTeleportPayloadS2C payload, ClientPlayNetworking.Context context) {
            if (context.client().currentScreen instanceof UnitScreen) {
            }
        }
    }
}
