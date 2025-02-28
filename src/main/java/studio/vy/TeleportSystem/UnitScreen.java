package studio.vy.TeleportSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class UnitScreen extends Screen {
    private final List<SpaceUnit> units = new ArrayList<>();
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    public UnitScreen() {
        super(Text.translatable("gui.blossom.teleport.title"));
    }

    @Override
    protected void init() {
        super.init();
        units.clear();
        units.addAll(CreateUnitScreen.storage.getAllUnits());
        // Add Create button at the top
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.create_new_unit"),
                        button -> {
                            MinecraftClient.getInstance().setScreen(new CreateUnitScreen(this));
                        })
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, 20, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build()
        );

        // List existing units below
        int y = 50;
        for (SpaceUnit unit : units) {
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal(unit.name()),
                            button -> {
                                teleport(unit);
                            })
                    .dimensions(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH/3, BUTTON_HEIGHT)
                    .build()
            );

            y += BUTTON_HEIGHT + 5;
        }
    }

    private void teleport(SpaceUnit unit) {
        assert client != null;
        if (client.isInSingleplayer()) {
            if (client.player != null) {
                unit.teleport(client.player);
                client.player.closeScreen();
            }
        }
        else {
            sendTeleportPacket(unit);
        }
    }

    private void sendTeleportPacket(SpaceUnit unit) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context,mouseX,mouseY,delta);
        super.render(context, mouseX, mouseY, delta);
    }
}
