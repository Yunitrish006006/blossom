package studio.vy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import studio.vy.TeleportSystem.ModKeybinds;
import studio.vy.TeleportSystem.SpaceUnit;
import studio.vy.TeleportSystem.UnitScreen;
import studio.vy.TeleportSystem.UnitTeleportPayloadS2C;

import java.util.ArrayList;

public class BlossomClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UnitTeleportPayloadS2C.ID, new UnitTeleportPayloadS2C.Receiver());

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
