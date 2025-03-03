package studio.vy.TeleportSystem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

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

    public BlockPos getBlock() {
        return BlockPos.ofFloored(x,y,z);
    }

    public ChunkPos getChunk(ServerWorld world) {
        return world.getChunk(BlockPos.ofFloored(x,y,z)).getPos();
    }

    public boolean isChunkLoaded(ServerWorld world) {
        return world.isChunkLoaded(getChunk(world).x, getChunk(world).z);
    }

    public boolean loadChunk(ServerWorld world) {
        if (isChunkLoaded(world)) return true;
        world.getChunkManager().addTicket(ChunkTicketType.PORTAL, getChunk(world), 1, null);
        return true;
    }

    public void unloadChunk(ServerWorld world) {
        if (!isChunkLoaded(world)) return;
        world.getChunkManager().removeTicket(ChunkTicketType.PORTAL, getChunk(world), 1, null);
    }

    public void teleport(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (owner.equals(id) || admin.contains(id) || allowed.contains(id)) {
            Vec3d from = player.getPos();
            Vec3d to = new Vec3d(x, y, z);
            ServerWorld world = player.getServerWorld();
            if (loadChunk(world)) {
                if (player.teleport(x, y, z, true)) {
                    player.sendMessage(Text.translatable("teleport to chunk " + getChunk(world).x + " " + getChunk(world).z + " from " + player.getChunkPos().x + " " + player.getChunkPos()), true);
                    player.sendMessage(Text.translatable("Traveled for " + from.distanceTo(to) + " blocks"), true);
                } else {
                    player.sendMessage(Text.translatable("Teleport failed - make sure the destination is loaded"), true);
                }
            }
        }
        else {
            player.sendMessage(Text.of("You are not allowed to teleport to this unit"), true);
        }
    }
}
