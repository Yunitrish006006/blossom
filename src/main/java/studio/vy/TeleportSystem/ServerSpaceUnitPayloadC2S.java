package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ServerSpaceUnitPayloadC2S(String operation, SpaceUnit unit) implements CustomPayload {
    public static final Id<ServerSpaceUnitPayloadC2S> ID = CustomPayload.id("server_space_unit");
    public static final PacketCodec<PacketByteBuf, ServerSpaceUnitPayloadC2S> CODEC = PacketCodec.of(ServerSpaceUnitPayloadC2S::encode, ServerSpaceUnitPayloadC2S::decode);

    public void encode(PacketByteBuf buf) {
        buf.writeString(operation);
        buf.writeString(unit.name());
        buf.writeDouble(unit.x());
        buf.writeDouble(unit.y());
        buf.writeDouble(unit.z());
        buf.writeString(unit.dimension());
        buf.writeUuid(unit.owner());
        buf.writeCollection(unit.admin(), (buffer, uuid) -> buffer.writeUuid(uuid));
        buf.writeCollection(unit.allowed(), (buffer, uuid) -> buffer.writeUuid(uuid));
    }

    public static ServerSpaceUnitPayloadC2S decode(PacketByteBuf buf) {
        String operation = buf.readString();
        String name = buf.readString();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String dimension = buf.readString();
        UUID owner = buf.readUuid();
        List<UUID> admin = buf.readCollection(ArrayList::new, (buffer) -> buffer.readUuid());
        List<UUID> allowed = buf.readCollection(ArrayList::new, (buffer) -> buffer.readUuid());
        return new ServerSpaceUnitPayloadC2S(operation, new SpaceUnit(name, x, y, z, dimension, owner, admin, allowed));
    }

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
