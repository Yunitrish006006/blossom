package studio.vy.TeleportSystem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner, List<UUID> admin,List<UUID> allowed) {
    public static final PacketCodec<ByteBuf, SpaceUnit> PACKET_CODEC = PacketCodec.ofStatic(SpaceUnit::encode, SpaceUnit::decode);
    private static void encode(ByteBuf buf, SpaceUnit unit) {
        PacketByteBuf packetByteBuf = buf instanceof PacketByteBuf ? (PacketByteBuf) buf : new PacketByteBuf(buf);
        packetByteBuf.writeString(unit.name());
        packetByteBuf.writeDouble(unit.x());
        packetByteBuf.writeDouble(unit.y());
        packetByteBuf.writeDouble(unit.z());
        packetByteBuf.writeString(unit.dimension());
        packetByteBuf.writeUuid(unit.owner());
        packetByteBuf.writeCollection(unit.admin(), (buffer, uuid) -> buffer.writeUuid(uuid));
        packetByteBuf.writeCollection(unit.allowed(), (buffer, uuid) -> buffer.writeUuid(uuid));
    }

    private static SpaceUnit decode(ByteBuf buf) {
        PacketByteBuf packetByteBuf = buf instanceof PacketByteBuf ? (PacketByteBuf) buf : new PacketByteBuf(buf);
        String name = packetByteBuf.readString();
        double x = packetByteBuf.readDouble();
        double y = packetByteBuf.readDouble();
        double z = packetByteBuf.readDouble();
        String dimension = packetByteBuf.readString();
        UUID owner = packetByteBuf.readUuid();
        List<UUID> admin = packetByteBuf.readCollection(ArrayList::new, buffer -> buffer.readUuid());
        List<UUID> allowed = packetByteBuf.readCollection(ArrayList::new, buffer -> buffer.readUuid());
        return new SpaceUnit(name, x, y, z, dimension, owner, admin, allowed);
    }
    public SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner) {
        this(name, x, y, z, dimension, owner, List.of(owner), List.of(owner));
    }

    public void teleport(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (owner.equals(id) || admin.contains(id) || allowed.contains(id)) {
            Vec3d pos = player.getPos();
            if (player.teleport(x, y, z, true)) {
                player.sendMessage(Text.of("Traveled for " + Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2) + Math.pow(z - pos.z, 2)) + " blocks"), false);
            } else {
                player.sendMessage(Text.of("Teleport failed - make sure the destination is loaded"), false);
            }
        }
        else {
            player.sendMessage(Text.of("You are not allowed to teleport to this unit"), false);
        }
    }
}
