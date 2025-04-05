package studio.vy.TeleportSystem.Component;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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

    public SpaceUnit(String name, ServerPlayerEntity player) {
        this(name, player.getX(), player.getY(), player.getZ(), player.getServerWorld().getRegistryKey().getValue().toString(), player.getUuid());
    }

    public SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner) {
        this(name, x, y, z, dimension, owner,
                new ArrayList<>(List.of(owner)),
                new ArrayList<>(List.of(owner)));
    }
    public SpaceUnit(ServerPlayerEntity player, ServerPlayerEntity requestPlayer) {
        this("request", player.getX(), player.getY(), player.getZ(), player.getServerWorld().getRegistryKey().getValue().toString(), player.getUuid());
        this.allowed.clear();
        this.allowed.add(requestPlayer.getUuid());
    }

    public SpaceUnit(String name, Vec3d position, String dimension, UUID owner) {
        this(name, position.x, position.y, position.z, dimension, owner);
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos((int) x >> 4, (int) z >> 4);
    }

    public ServerWorld getWorld(ServerPlayerEntity player) {
        return player.getServerWorld();
    }

    public Vec3d getVecPos() {
        return new Vec3d(x, y, z);
    }

    public BlockPos getBlockPos() {
        return BlockPos.ofFloored(x,y,x);
    }

    public void teleport(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (!(owner.equals(id) || admin.contains(id) || allowed.contains(id))) {
            player.sendMessage(Text.of("You are not allowed to teleport to this unit"), true);
            return;
        }

        ServerWorld world = getWorld(player);
        ChunkPos chunkPos = getChunkPos();
        Vec3d from = player.getPos();
        Vec3d destination = getVecPos();

        double distance = from.distanceTo(destination);
        int teleportCost = Math.round((float) ( Math.log(distance) / Math.log(4)));

        new Thread(() -> {
            try {
                for (int i = teleportCost; i > 0; i--) {
                    final int count = i;
                    player.getServer().submit(() ->
                            player.sendMessage(Text.of("Teleporting in " + count + "..."), true)
                    );
                    Thread.sleep(1000);
                }

                // 倒數結束後執行傳送
                player.getServer().submitAndJoin(() -> {
                    loadChunk(world, chunkPos);
                    if (player.teleport(world, x, y, z, PositionFlag.getFlags(0), player.getYaw(), player.getPitch(), true)) {
                        player.sendMessage(Text.of("Traveled for " + distance + " blocks"), true);
                    } else {
                        player.sendMessage(Text.of("Teleport failed"), true);
                    }
                });
            } catch (Exception e) {
                player.sendMessage(Text.of("Teleport cancelled"), true);
            }
        }).start();
    }

    public void loadChunk(ServerWorld world, ChunkPos pos) {
        world.getChunkManager().addTicket(
                ChunkTicketType.PORTAL,
                pos,
                1
        );
    }
}
