package studio.vy.TeleportSystem;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static final String CATEGORY = "key.categories.blossom";
    public static KeyBinding openUnitScreen;

    public static void register() {
        openUnitScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blossom.open_unit_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                CATEGORY
        ));
    }
}
