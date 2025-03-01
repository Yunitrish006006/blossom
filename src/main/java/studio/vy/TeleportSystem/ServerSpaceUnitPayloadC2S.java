package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record ServerSpaceUnitPayloadC2S(String operation, SpaceUnit unit) implements CustomPayload {
    public static final Id<ServerSpaceUnitPayloadC2S> ID = CustomPayload.id("server_space_unit");
    public static final PacketCodec<PacketByteBuf, ServerSpaceUnitPayloadC2S> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            ServerSpaceUnitPayloadC2S::operation,
            SpaceUnit.PACKET_CODEC.cast(),
            ServerSpaceUnitPayloadC2S::unit,
            ServerSpaceUnitPayloadC2S::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(String operation, SpaceUnit unit) {
        ClientPlayNetworking.send(new ServerSpaceUnitPayloadC2S(operation, unit));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<ServerSpaceUnitPayloadC2S> {
        @Override
        public void receive(ServerSpaceUnitPayloadC2S payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (player != null) {
                ServerSpaceUnitManager manager = ServerSpaceUnitManager.getInstance(player.getServer());

                // 檢查權限
                if (payload.unit().owner().equals(player.getUuid())) {
                    switch (payload.operation()) {
                        case "add" -> manager.addUnit(payload.unit());
                        case "remove" -> manager.removeUnit(payload.unit());
                    }

                    // 同步更新到所有在線玩家
                    for (ServerPlayerEntity onlinePlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        List<SpaceUnit> visibleUnits = manager.getVisibleUnits(onlinePlayer.getUuid());
                        SpaceUnitSyncPayloadS2C.send(onlinePlayer, visibleUnits);
                    }
                }
            }
        }
    }
}
