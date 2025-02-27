package studio.vy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import studio.vy.TeleportSystem.ModKeybinds;
import studio.vy.TeleportSystem.SpaceUnit;
import studio.vy.TeleportSystem.UnitScreen;

import java.util.ArrayList;

public class BlossomClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {

        ModKeybinds.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeybinds.openUnitScreen.wasPressed()) {
                openUnitScreen(client);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("units")
                    .executes(context -> {
                        openUnitScreen(MinecraftClient.getInstance());
                        return 1;
                    }));
        });

    }
    private void openUnitScreen(MinecraftClient client) {
        ArrayList<SpaceUnit> units = new ArrayList<>();
        units.add(new SpaceUnit("home",0,120, 0, "minecraft:overworld",null));
        client.setScreen(new UnitScreen(units));
    }
}
