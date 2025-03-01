package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SpaceUnitSyncPayloadS2C(List<SpaceUnit> units) implements CustomPayload {
    public static final Id<SpaceUnitSyncPayloadS2C> ID = CustomPayload.id("space_unit_sync");
    public static final PacketCodec<PacketByteBuf, SpaceUnitSyncPayloadS2C> CODEC = PacketCodec.tuple(
            new PacketCodec<>() {
                public void encode(PacketByteBuf buf, List<SpaceUnit> units) {
                    buf.writeCollection(units, SpaceUnit.PACKET_CODEC);
                }
                public List<SpaceUnit> decode(PacketByteBuf buf) {
                    return buf.readCollection(ArrayList::new, SpaceUnit.PACKET_CODEC);
                }
            },
            SpaceUnitSyncPayloadS2C::units,
            SpaceUnitSyncPayloadS2C::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, List<SpaceUnit> units) {
        ServerPlayNetworking.send(player, new SpaceUnitSyncPayloadS2C(units));
    }
}
