package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static final String CATEGORY = "key.categories.blossom";
    public static KeyBinding openUnitScreen;
    public static KeyBinding flightKey;

    public static void register() {
        openUnitScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blossom.open_unit_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                CATEGORY
        ));

        flightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blossom.flight",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.blossom.general"
        ));

        // Add tick event to handle key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (flightKey.wasPressed()) {
                if (client.player != null) {
                    boolean currentFlight = client.player.getAbilities().allowFlying;
                    client.player.getAbilities().allowFlying = !currentFlight;
                    client.player.getAbilities().flying = false;
                    client.player.sendAbilitiesUpdate();
                }
            }
        });
    }
}
