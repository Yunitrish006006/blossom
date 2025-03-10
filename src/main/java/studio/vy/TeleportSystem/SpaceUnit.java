package studio.vy.TeleportSystem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner, List<UUID> admin,List<UUID> allowed) {
    public static final PacketCodec<ByteBuf, SpaceUnit> PACKET_CODEC = PacketCodec.ofStatic(SpaceUnit::encode, SpaceUnit::decode);

    public static SpaceUnit ERROR = new SpaceUnit("error", 0, 0, 0, "error", UUID.randomUUID());

    private static void encode(ByteBuf buf, SpaceUnit unit) {
        if (unit == null) {
            unit = ERROR;
        }
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

    public boolean loadChunk(ServerWorld world, ChunkPos pos) {
        if (world.isChunkLoaded(pos.x, pos.z)) return true;
        world.getChunkManager().addTicket(ChunkTicketType.PLAYER, pos, 1 , pos);
        return world.isChunkLoaded(pos.x, pos.z);
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos((int) x >> 4, (int) z >> 4);
    }

    public ServerWorld getWorld(ServerPlayerEntity player) {
        return player.getServerWorld();
    }

    public Vec3d getPos() {
        return new Vec3d(x, y, z);
    }

    public void teleport(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (!(owner.equals(id) || admin.contains(id) || allowed.contains(id))) {
            player.sendMessage(Text.of("You are not allowed to teleport to this unit"), true);
            return;
        }

        ServerWorld world = getWorld(player);
        ChunkPos chunkPos = getChunkPos();
        player.sendMessage(Text.of("Loading chunk " + chunkPos.x + " " + chunkPos.z), true);
        player.sendMessage(Text.of("Teleporting in 3 seconds..."), true);
        world.getServer().execute(() -> loadChunk(world, chunkPos));
        world.getServer().execute(() -> {
            try {
                Thread.sleep(3000);
                Vec3d from = player.getPos();
                Vec3d destination = getPos();
                if (player.teleport(x, y, z, true)) {
                    player.sendMessage(Text.of("Traveled for " + from.distanceTo(destination) + " blocks"), true);
                } else {
                    player.sendMessage(Text.of("Teleport failed... retrying"), true);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
//        if (loadChunk(world, chunkPos)) {
//
//        } else {
//            player.sendMessage(Text.of("Teleport failed, please retry"), true);
//        }
    }
}
