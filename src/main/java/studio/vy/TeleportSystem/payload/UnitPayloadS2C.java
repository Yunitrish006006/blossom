package studio.vy.TeleportSystem.payload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import studio.vy.TeleportSystem.SpaceUnit;
import studio.vy.TeleportSystem.screen.UnitList;

import java.util.ArrayList;
import java.util.List;

public record UnitPayloadS2C(List<SpaceUnit> units) implements CustomPayload {
    public static final Id<UnitPayloadS2C> ID = CustomPayload.id("unit_s2c");
    public static final PacketCodec<PacketByteBuf, UnitPayloadS2C> CODEC = PacketCodec.tuple(
            new PacketCodec<>() {
                public void encode(PacketByteBuf buf, List<SpaceUnit> units) {
                    buf.writeCollection(units, SpaceUnit.PACKET_CODEC);
                }
                public List<SpaceUnit> decode(PacketByteBuf buf) {
                    return buf.readCollection(ArrayList::new, SpaceUnit.PACKET_CODEC);
                }
            },
            UnitPayloadS2C::units,
            UnitPayloadS2C::new
    );
    public static void send(ServerPlayerEntity player, List<SpaceUnit> units) {
        ServerPlayNetworking.send(player, new UnitPayloadS2C(units));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<UnitPayloadS2C> {
        @Override
        public void receive(UnitPayloadS2C payload, ClientPlayNetworking.Context context) {
            if (context.client().currentScreen instanceof UnitList) {
                UnitList unitList = (UnitList) context.client().currentScreen;
                unitList.refresh(payload.units());
            }
        }
    }
}
