package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record UnitTeleportPayloadC2S(SpaceUnit unit) implements CustomPayload {

    public static final Id<UnitTeleportPayloadC2S> ID = CustomPayload.id("unit_teleport_c2s");
    public static final PacketCodec<PacketByteBuf, UnitTeleportPayloadC2S> CODEC = PacketCodec.tuple(
            SpaceUnit.PACKET_CODEC.cast(),
            UnitTeleportPayloadC2S::unit,
            UnitTeleportPayloadC2S::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(SpaceUnit unit) {
        ClientPlayNetworking.send(new UnitTeleportPayloadC2S(unit));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<UnitTeleportPayloadC2S>{
        @Override
        public void receive(UnitTeleportPayloadC2S payload, ServerPlayNetworking.Context context) {
            if(context.player() != null){
                context.player().sendMessage(Text.translatable("Receive package"), false);
                payload.unit().teleport(context.player());
            }
        }
    }
}
