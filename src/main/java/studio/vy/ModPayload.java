package studio.vy;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import studio.vy.TeleportSystem.ServerSpaceUnitPayloadC2S;
import studio.vy.TeleportSystem.SpaceUnitSyncPayloadS2C;
import studio.vy.TeleportSystem.UnitTeleportPayloadC2S;
import studio.vy.TeleportSystem.UnitTeleportPayloadS2C;

public class ModPayload {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(UnitTeleportPayloadC2S.ID, UnitTeleportPayloadC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(UnitTeleportPayloadS2C.ID, UnitTeleportPayloadS2C.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerSpaceUnitPayloadC2S.ID, ServerSpaceUnitPayloadC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(SpaceUnitSyncPayloadS2C.ID, SpaceUnitSyncPayloadS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UnitTeleportPayloadC2S.ID, new UnitTeleportPayloadC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(ServerSpaceUnitPayloadC2S.ID, new ServerSpaceUnitPayloadC2S.Receiver());
    }
}
