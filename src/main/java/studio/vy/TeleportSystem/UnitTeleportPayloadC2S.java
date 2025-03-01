package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record UnitTeleportPayloadC2S(SpaceUnit unit) implements CustomPayload {

    public static final Id<UnitTeleportPayloadC2S> ID = CustomPayload.id("unit_teleport_c2s");
    public static final PacketCodec<PacketByteBuf, UnitTeleportPayloadC2S> CODEC = PacketCodec.of(UnitTeleportPayloadC2S::encode, UnitTeleportPayloadC2S::decode);

    public void encode(PacketByteBuf buf) {
        buf.writeString(unit.name());
        buf.writeDouble(unit.x());
        buf.writeDouble(unit.y());
        buf.writeDouble(unit.z());
        buf.writeString(unit.dimension());
        buf.writeUuid(unit.owner());
        buf.writeCollection(unit.admin(), (buffer, uuid) -> buffer.writeUuid(uuid));
        buf.writeCollection(unit.allowed(), (buffer, uuid) -> buffer.writeUuid(uuid));
    }

    public static UnitTeleportPayloadC2S decode(PacketByteBuf buf) {
        String name = buf.readString();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String dimension = buf.readString();
        UUID owner = buf.readUuid();
        List<UUID> admin = buf.readCollection(ArrayList::new, (buffer) -> buffer.readUuid());
        List<UUID> allowed = buf.readCollection(ArrayList::new, (buffer) -> buffer.readUuid());
        return new UnitTeleportPayloadC2S(new SpaceUnit(name, x, y, z, dimension, owner, admin, allowed));
    }


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(SpaceUnit unit) {
        ClientPlayNetworking.send(new UnitTeleportPayloadC2S(unit));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<UnitTeleportPayloadC2S>{
        @Override
        public void receive(UnitTeleportPayloadC2S payload, ServerPlayNetworking.Context context) {
            if(context.player() != null){
                context.player().sendMessage(Text.translatable("Receive package"), false);
                payload.unit().teleport(context.player());
            }
        }
    }
}
