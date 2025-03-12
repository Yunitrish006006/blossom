package studio.vy.TeleportSystem.payload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import studio.vy.TeleportSystem.SpaceUnit;
import studio.vy.TeleportSystem.SpaceUnitManager;

import java.util.List;

public record UnitPayloadC2S(String operation, SpaceUnit unit) implements CustomPayload {
    public static final Id<UnitPayloadC2S> ID = CustomPayload.id("server_space_unit");
    public static final PacketCodec<PacketByteBuf, UnitPayloadC2S> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            UnitPayloadC2S::operation,
            SpaceUnit.PACKET_CODEC.cast(),
            UnitPayloadC2S::unit,
            UnitPayloadC2S::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(String operation, SpaceUnit unit) {
        ClientPlayNetworking.send(new UnitPayloadC2S(operation, unit));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<UnitPayloadC2S> {
        @Override
        public void receive(UnitPayloadC2S payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (player != null) {
                SpaceUnitManager manager = SpaceUnitManager.getServerInstance(player.getServer());

                switch (payload.operation()) {
                    case "add" -> {
                        if (payload.unit().owner().equals(player.getUuid())) {
                            manager.addUnit(payload.unit());
                        }
                    }
                    case "remove" -> {
                        if (payload.unit().owner().equals(player.getUuid())) {
                            manager.removeUnit(payload.unit());
                        }
                    }
                    case "teleport" -> payload.unit.teleport(player);
                    case "fetch_owned" -> {
                        List<SpaceUnit> ownedUnits = manager.config.getOwned(player.getUuid());
                        UnitPayloadS2C.send(player, ownedUnits);
                    }
                    case "fetch_allowed" -> {
                        List<SpaceUnit> allowedUnits = manager.config.getAllowed(player.getUuid());
                        UnitPayloadS2C.send(player, allowedUnits);
                    }
                    case "fetch_editable" -> {
                        List<SpaceUnit> editableUnits = manager.config.getEditable(player.getUuid());
                        UnitPayloadS2C.send(player, editableUnits);
                    }
                    case "fetch_all" -> {
                        List<SpaceUnit> allUnits = manager.config.units;
                        UnitPayloadS2C.send(player, allUnits);
                    }
                }
            }
        }
    }
}
