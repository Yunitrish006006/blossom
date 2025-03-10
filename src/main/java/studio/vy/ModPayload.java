package studio.vy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.payload.UnitPayloadS2C;

public class ModPayload {
    public static void initCommon() {
        PayloadTypeRegistry.playC2S().register(UnitPayloadC2S.ID, UnitPayloadC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(UnitPayloadS2C.ID, UnitPayloadS2C.CODEC);
    }

    public static void initServer() {
        ServerPlayNetworking.registerGlobalReceiver(UnitPayloadC2S.ID, new UnitPayloadC2S.Receiver());
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(UnitPayloadS2C.ID, new UnitPayloadS2C.Receiver());
    }
}
