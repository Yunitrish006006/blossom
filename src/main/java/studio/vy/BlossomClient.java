package studio.vy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import studio.vy.TeleportSystem.*;
import studio.vy.TeleportSystem.payload.UnitPayloadS2C;
import studio.vy.TeleportSystem.screen.UnitList;

import java.util.List;

public class BlossomClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UnitPayloadS2C.ID, new UnitPayloadS2C.Receiver());
        ModPayload.initClient();
        ModKeybinds.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeybinds.openUnitScreen.wasPressed()) {
                client.setScreen(new UnitList(
//
                        List.of()
//
                ));
            }
        });

//        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("units")
//                .executes(context -> {
//                    MinecraftClient.getInstance().setScreen(new UnitList());
//                    return 1;
//                })));


    }
}
