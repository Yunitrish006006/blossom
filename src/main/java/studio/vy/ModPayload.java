package studio.vy;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import studio.vy.TeleportSystem.UnitTeleportPayloadC2S;
import studio.vy.TeleportSystem.UnitTeleportPayloadS2C;

public class ModPayload {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(UnitTeleportPayloadC2S.ID, UnitTeleportPayloadC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(UnitTeleportPayloadS2C.ID, UnitTeleportPayloadS2C.CODEC);
    }
}
