package studio.vy.TeleportSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class UnitScreen extends Screen {
    private final List<SpaceUnit> units = new ArrayList<>();
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static UnitScreen currentScreen;

    public UnitScreen() {
        super(Text.translatable("gui.blossom.teleport.title"));
    }

    @Override
    public void close() {
        currentScreen = null;
        super.close();
    }

    public static void refresh() {
        if (currentScreen != null) {
            currentScreen.init();
        }
    }

    @Override
    protected void init() {
        super.init();
        units.clear();
//        units.addAll(CreateUnitScreen.storage.getAllUnits());
        if (client != null) {
            if (client.player != null) {
                units.addAll(CreateUnitScreen.storage.getOwnedUnits(client.player.getUuid()));
            }
        }
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

            // Delete button
            this.addDrawableChild(ButtonWidget.builder(
                            Text.translatable("gui.blossom.teleport.delete"),
                            button -> deleteUnit(unit))
                    .dimensions(this.width / 2 - BUTTON_WIDTH / 2 + BUTTON_WIDTH/3 + 5, y, BUTTON_WIDTH/3, BUTTON_HEIGHT)
                    .build()
            );

            y += BUTTON_HEIGHT + 5;
        }
    }

    private void deleteUnit(SpaceUnit unit) {
        assert client != null;
        if (client.player != null && unit.owner().equals(client.player.getUuid())) {
            if (client.isInSingleplayer()) {
                CreateUnitScreen.storage.removeUnit(unit);
                refresh(); // 單人遊戲直接重新整理
            } else {
                ServerSpaceUnitPayloadC2S.send("remove", unit);
                // 多人遊戲等待伺服器回應後重新整理
            }
        }
    }

    private void teleport(SpaceUnit unit) {
        assert client != null;
        if (client.isInSingleplayer()) {
            if (client.player != null && client.getServer() != null) {
                ServerPlayerEntity serverPlayer = client.getServer().getPlayerManager().getPlayer(client.player.getUuid());
                if (serverPlayer != null) {
                    unit.teleport(serverPlayer);
                }
                client.player.closeScreen();
            }
        }
        else {
            UnitTeleportPayloadC2S.send(unit);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context,mouseX,mouseY,delta);
        super.render(context, mouseX, mouseY, delta);
    }
}
