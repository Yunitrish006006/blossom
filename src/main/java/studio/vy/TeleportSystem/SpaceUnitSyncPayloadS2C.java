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
    public static final PacketCodec<PacketByteBuf, SpaceUnitSyncPayloadS2C> CODEC = PacketCodec.of(SpaceUnitSyncPayloadS2C::encode, SpaceUnitSyncPayloadS2C::decode);

    public void encode(PacketByteBuf buf) {
        buf.writeCollection(units, (buffer, unit) -> {
            buffer.writeString(unit.name());
            buffer.writeDouble(unit.x());
            buffer.writeDouble(unit.y());
            buffer.writeDouble(unit.z());
            buffer.writeString(unit.dimension());
            buffer.writeUuid(unit.owner());
            buf.writeCollection(unit.admin(), (buffer2, uuid) -> buffer2.writeUuid(uuid));
            buf.writeCollection(unit.allowed(), (buffer2, uuid) -> buffer2.writeUuid(uuid));
        });
    }

    public static SpaceUnitSyncPayloadS2C decode(PacketByteBuf buf) {
        List<SpaceUnit> units = buf.readCollection(ArrayList::new, buffer -> {
            String name = buffer.readString();
            double x = buffer.readDouble();
            double y = buffer.readDouble();
            double z = buffer.readDouble();
            String dimension = buffer.readString();
            UUID owner = buffer.readUuid();
            List<UUID> admin = buf.readCollection(ArrayList::new, (buffer2) -> buffer2.readUuid());
            List<UUID> allowed = buf.readCollection(ArrayList::new, (buffer2) -> buffer2.readUuid());
            return new SpaceUnit(name, x, y, z, dimension, owner, admin, allowed);
        });
        return new SpaceUnitSyncPayloadS2C(units);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, List<SpaceUnit> units) {
        ServerPlayNetworking.send(player, new SpaceUnitSyncPayloadS2C(units));
    }
}
