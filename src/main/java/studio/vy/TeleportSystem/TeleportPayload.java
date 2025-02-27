package studio.vy.TeleportSystem;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import studio.vy.Blossom;

public record TeleportPayload(String name, double x, double y, double z, String dimension) implements CustomPayload {
    public static final Identifier PACKET_ID = Blossom.identifier("teleport");
    public static final Id<TeleportPayload> ID = CustomPayload.id(PACKET_ID.toString());

    public static TeleportPayload read(PacketByteBuf buf) {
        return new TeleportPayload(
                buf.readString(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readString()
        );
    }
    public void write(PacketByteBuf buf) {
        buf.writeString(name);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeString(dimension);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        System.out.println("ID: " + ID);
        return ID;
    }
}
