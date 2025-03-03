package studio.vy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import studio.vy.TeleportSystem.*;

import java.util.ArrayList;

public class BlossomClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UnitTeleportPayloadS2C.ID, new UnitTeleportPayloadS2C.Receiver());

        // 添加同步資料的接收器
        ClientPlayNetworking.registerGlobalReceiver(SpaceUnitSyncPayloadS2C.ID, (payload, context) -> {
            if (CreateUnitScreen.storage != null) {
                context.client().execute(() -> {
                    // 清空現有資料並更新為伺服器傳來的資料
                    CreateUnitScreen.storage.clearAndSetUnits(payload.units());
                    // 重新整理畫面
                    UnitScreen.refresh();
                });
            }
        });

        ModKeybinds.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeybinds.openUnitScreen.wasPressed()) {
                client.setScreen(new UnitScreen());
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("units")
                .executes(context -> {
                    MinecraftClient.getInstance().setScreen(new UnitScreen());
                    return 1;
                })));


    }
}
