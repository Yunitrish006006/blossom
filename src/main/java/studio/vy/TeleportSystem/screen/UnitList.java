package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.SpaceUnit;
import studio.vy.TeleportSystem.SpaceUnitManager;

import java.util.ArrayList;
import java.util.List;

public class UnitList extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private List<SpaceUnit> units;
    public static String page = "owned";

    public UnitList(List<SpaceUnit> units) {
        super(Text.translatable("gui.blossom.teleport.title"));
        this.units = units;
    }

    public void refresh(List<SpaceUnit> units) {
        this.units = new ArrayList<>();
        init();
        if (units==null) {
            this.units.add(new SpaceUnit("world_spawn", 0, 0, 0, "minecraft:overworld", MinecraftClient.getInstance().player.getUuid()));
        }
        else {
            this.units.clear();
            this.units.addAll(units);
        }
    }

    private void editPermission(SpaceUnit unit) {
        assert client != null;
        if (client.player != null && unit.owner().equals(client.player.getUuid())) {
            client.setScreen(new EditUnit(this, unit));
        }
    }

    private ButtonWidget createNewUnitButton(int y) {
        return ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.create_new_unit"),
                        button -> {
                            MinecraftClient.getInstance().setScreen(new CreateUnit(this));
                        })
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }

    private ButtonWidget teleportUnitButton(SpaceUnit unit, int y) {
        return ButtonWidget.builder(
                        Text.literal(unit.name()),
                        button -> {
                            teleport(unit);
                        })
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }

    private ButtonWidget deleteUnitButton(SpaceUnit unit, int y) {
        return ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.delete"),
                        button -> deleteUnit(unit))
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2 + BUTTON_WIDTH/3 + 5, y, BUTTON_WIDTH/3, BUTTON_HEIGHT)
                .build();
    }

    private ButtonWidget editUnitButton(SpaceUnit unit, int y) {
        return ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.edit"),
                        button -> editPermission(unit))
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2 + BUTTON_WIDTH/3 + 5, y, BUTTON_WIDTH/3, BUTTON_HEIGHT)
                .build();
    }

    private void renderOwnUnitPage() {
        int y = 50;
        this.addDrawableChild(createNewUnitButton(y));
        y+=20;
        for (SpaceUnit unit : units) {
            this.addDrawableChild(teleportUnitButton(unit, y));
            this.addDrawableChild(deleteUnitButton(unit, y));
            this.addDrawableChild(editUnitButton(unit, y));
            y += BUTTON_HEIGHT + 5;
        }
    }

    private void renderPageButtonRow() {
        this.addDrawableChild(
                this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.owned_units"),
                        button -> {
                            page = "owned";
                            clearAndInit();
                        }
                        )
                        .dimensions(this.width / 4 - BUTTON_WIDTH / 4, 20, BUTTON_WIDTH/4, BUTTON_HEIGHT)
                        .build()
                )
        );
        this.addDrawableChild(
                this.addDrawableChild(ButtonWidget.builder(
                                        Text.translatable("gui.blossom.teleport.all_units"),
                                        button -> {
                                            page = "all";
                                            clearAndInit();
                                        }
                                )
                                .dimensions(this.width / 2 - BUTTON_WIDTH / 4, 20, BUTTON_WIDTH/4, BUTTON_HEIGHT)
                                .build()
                )
        );
    }

    private void renderAllUnitPage() {
        int y = 70;
        for (SpaceUnit unit : units) {
            this.addDrawableChild(teleportUnitButton(unit, y));
            y += BUTTON_HEIGHT + 5;
        }
    }

    private void fetchAllUnits() {
        if (client != null && client.player != null) {
            if (client.isInSingleplayer()) {
                SpaceUnitManager manager = SpaceUnitManager.getClientInstance(client.getServer());
                units = manager.getAllUnits();
            } else {
                UnitPayloadC2S.send("fetch_all", null);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        renderPageButtonRow();
        System.out.println("Current page:" + page);
        switch (page) {
            case "owned":
                renderOwnUnitPage();
                break;
            case "admin":
                // renderAdminUnitPage();
                break;
            case "allowed":
                // renderAllowedUnitPage();
                break;
            case "all":
                fetchAllUnits();
                renderAllUnitPage();
                break;
        }
    }

    private void deleteUnit(SpaceUnit unit) {
        assert client != null;
        if (client.player != null && unit.owner().equals(client.player.getUuid())) {
            if (client.isInSingleplayer()) {
                SpaceUnitManager.getClientInstance(client.getServer()).removeUnit(unit);
                units.remove(unit);
                refresh(units);
            } else {
                UnitPayloadC2S.send("remove", unit);
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
            UnitPayloadC2S.send("teleport", unit);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context,mouseX,mouseY,delta);
        super.render(context, mouseX, mouseY, delta);
    }
}
