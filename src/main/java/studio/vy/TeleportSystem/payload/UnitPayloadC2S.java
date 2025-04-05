package studio.vy.TeleportSystem.payload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import studio.vy.TeleportSystem.Component.SpaceUnit;
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
                SpaceUnitManager manager = SpaceUnitManager.getServerInstance();

                switch (payload.operation()) {
                    case "add" -> {
                        if (payload.unit().owner().equals(player.getUuid())) {
                            manager.addUnit(payload.unit());
                            List<SpaceUnit> ownedUnits = manager.config.getOwned(player.getUuid());
                            UnitPayloadS2C.send("update", player, ownedUnits);
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
                        UnitPayloadS2C.send("update", player, ownedUnits);
                    }
                    case "fetch_allowed" -> {
                        List<SpaceUnit> allowedUnits = manager.config.getAllowed(player.getUuid());
                        UnitPayloadS2C.send("update", player, allowedUnits);
                    }
                    case "fetch_editable" -> {
                        List<SpaceUnit> editableUnits = manager.config.getEditable(player.getUuid());
                        UnitPayloadS2C.send("update", player, editableUnits);
                    }
                    case "fetch_all" -> {
                        List<SpaceUnit> allUnits = manager.config.units;
                        UnitPayloadS2C.send("update", player, allUnits);
                    }
                    case "player_teleport" -> {
                        PlayerManager playerManager = context.server().getPlayerManager();
                        ServerPlayerEntity target = playerManager.getPlayer(payload.unit().allowed().getFirst());
                        ServerPlayerEntity request = playerManager.getPlayer(payload.unit().admin().getFirst());
                        if (target == null || request == null) return;
                        SpaceUnit tempUnit = new SpaceUnit(request,target);
                        tempUnit.teleport(target);
                    }
                    case "temp_teleport" -> {
                        manager.addUnit(payload.unit());
                        payload.unit().teleport(player);
                        manager.removeUnit(payload.unit());
                    }
                    case "request_teleport" -> {
                        PlayerManager playerManager = context.server().getPlayerManager();
                        ServerPlayerEntity target = playerManager.getPlayer(payload.unit().admin().getFirst());
                        ServerPlayerEntity request = playerManager.getPlayer(payload.unit().allowed().getFirst());
                        if (target == null || request == null) return;
                        UnitPayloadS2C.sendTpRequest(request, target);
                    }
                    case "add_allowed" -> {
                        if (payload.unit() != null) {
                            manager.addAllowedPlayer(payload.unit(), player.getUuid());
                            List<SpaceUnit> ownedUnits = manager.config.getOwned(player.getUuid());
                            UnitPayloadS2C.send("update", player, ownedUnits);
                        }
                    }
                }
            }
        }
    }
}
