package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import studio.vy.Blossom;

public record UnitTeleportPayloadC2S() implements CustomPayload {

    public static final Id<UnitTeleportPayloadC2S> ID = CustomPayload.id("unit_teleport_c2s");
    public static final PacketCodec<PacketByteBuf, UnitTeleportPayloadC2S> CODEC = PacketCodec.unit(new UnitTeleportPayloadC2S());
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send() {
        ClientPlayNetworking.send(new UnitTeleportPayloadC2S());
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<UnitTeleportPayloadC2S>{
        @Override
        public void receive(UnitTeleportPayloadC2S payload, ServerPlayNetworking.Context context) {
            if(context.player() != null){
                context.player().sendMessage(Text.translatable("Receive package"), false);
            }
        }
    }
}
