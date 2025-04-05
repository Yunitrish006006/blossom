package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.Component.SpaceUnit;
import studio.vy.TeleportSystem.SpaceUnitManager;

import java.util.List;
import java.util.UUID;

public class UnitList extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private List<SpaceUnit> units;
    public static String page = "owned";

    public UnitList(List<SpaceUnit> units) {
        super(Text.translatable("gui.blossom.teleport.title"));
        this.units = units;
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
                        button -> MinecraftClient.getInstance().setScreen(new CreateUnit(this)))
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }

    private ButtonWidget teleportUnitButton(SpaceUnit unit, int y) {
        return teleportUnitButton(unit,8,y);
    }

    private ButtonWidget teleportUnitButton(SpaceUnit unit, int grid_x, int y) {
        return ButtonWidget.builder(
                        Text.literal(unit.name()),
                        button -> teleport(unit))
                .dimensions(
                        this.width*grid_x / 16 - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH / 3 - 5,
                        BUTTON_HEIGHT
                )
                .build();
    }

    private ButtonWidget deleteUnitButton(SpaceUnit unit, int y) {
        return ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.delete"),
                        button -> deleteUnit(unit))
                .dimensions(
                        this.width / 2 - BUTTON_WIDTH / 2 + 2 * (BUTTON_WIDTH / 3) + 5,
                        y,
                        BUTTON_WIDTH / 3 - 5,
                        BUTTON_HEIGHT
                )
                .build();
    }

    private ButtonWidget editUnitButton(SpaceUnit unit, int y) {
        return ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.edit"),
                        button -> editPermission(unit))
                .dimensions(
                        this.width / 2 - BUTTON_WIDTH / 2 + (BUTTON_WIDTH / 3),
                        y,
                        BUTTON_WIDTH / 3 - 5,
                        BUTTON_HEIGHT
                )
                .build();
    }

    private void renderPageButtonRow() {
        int x = this.width / 16;
        int y = 20;
        int buttonWidth = BUTTON_WIDTH/4;
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.owned_units"),
                        button -> {
                            page = "owned";
                            fetchOwnedUnits();
                            clearAndInit();
                        }
                ).dimensions(x, y, buttonWidth, BUTTON_HEIGHT)
                .build());

        x+=this.width/16+buttonWidth;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.all_units"),
                        button -> {
                            page = "all";
                            fetchAllUnits();
                            clearAndInit();
                        }
                ).dimensions(x, y, buttonWidth, BUTTON_HEIGHT)
                .build());
        x+=this.width/16+buttonWidth;
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.allowed_units"),
                        button -> {
                            page = "allowed";
                            fetchAllowedUnits();
                            clearAndInit();
                        }
                ).dimensions(x, y, buttonWidth, BUTTON_HEIGHT)
                .build());
        x+=this.width/16+buttonWidth;
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.players"),
                        button -> {
                            page = "players";
                            clearAndInit();
                        }
                ).dimensions(x, y, buttonWidth, BUTTON_HEIGHT)
                .build());
    }

    private void renderEditAbleTypePage(boolean canEdit) {
        int y = 50;
        this.addDrawableChild(createNewUnitButton(y));
        y += BUTTON_HEIGHT + 5;
        for (SpaceUnit unit : units) {
            this.addDrawableChild(teleportUnitButton(unit, y));
            if (canEdit) {
                this.addDrawableChild(editUnitButton(unit, y));
                this.addDrawableChild(deleteUnitButton(unit, y));
            }
            y += BUTTON_HEIGHT + 5;
        }
    }

    private void renderAllowedUnitPage() {
        int y = 70;
        int grid_x = 4;
        for (SpaceUnit unit : units) {
            this.addDrawableChild(teleportUnitButton(unit, grid_x, y));
            y += BUTTON_HEIGHT + 5;
            if (y > this.height - 50) {
                y = 70;
                grid_x += 3;
            }
        }
    }

    private void renderAllUnitPage() {
        int y = 70;
        int grid_x = 4;
        for (SpaceUnit unit : units) {
            this.addDrawableChild(teleportUnitButton(unit, grid_x, y));
            y += BUTTON_HEIGHT + 5;
            if (y > this.height - 50) {
                y = 70;
                grid_x += 3;
            }
        }
    }

    private void fetchAllUnits() {
        if (client==null) return;
        if (!client.isInSingleplayer()) {
            UnitPayloadC2S.send("fetch_all", null);
        } else if (client.player != null) {
            SpaceUnitManager manager = SpaceUnitManager.getClientInstance();
            units = manager.config.units;
        }
    }

    private void fetchAllowedUnits() {
        if (client==null) return;
        if (!client.isInSingleplayer()) {
            UnitPayloadC2S.send("fetch_allowed", null);
        } else if (client.player != null) {
            SpaceUnitManager manager = SpaceUnitManager.getClientInstance();
            units = manager.config.getAllowed(client.player.getUuid());
        }
    }

    private void fetchOwnedUnits() {
        if (client==null) return;
        if (!client.isInSingleplayer()) {
            UnitPayloadC2S.send("fetch_owned", null);
        } else if (client.player != null) {
            SpaceUnitManager manager = SpaceUnitManager.getClientInstance();
            units = manager.config.getOwned(client.player.getUuid());
        }
    }

    private void renderPlayerPage() {
        int y = 70;
        int grid_x = 4;
        if (client != null && client.getNetworkHandler() != null) {
            for (PlayerListEntry targetPlayer : client.getNetworkHandler().getPlayerList()) {
                if (client.player != null && targetPlayer.getProfile().getId().equals(client.player.getUuid())) {
                    continue;
                }

                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal(targetPlayer.getProfile().getName()),
                        button -> requestTeleportToPlayer(targetPlayer.getProfile().getId())
                ).dimensions(
                        this.width * grid_x / 16 - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH / 3 - 5,
                        BUTTON_HEIGHT
                ).build());

                y += BUTTON_HEIGHT + 5;
                if (y > this.height - 50) {
                    y = 70;
                    grid_x += 3;
                }
            }
        }
    }

    private void requestTeleportToPlayer(UUID targetId) {
        if (client == null || client.player == null) return;
        SpaceUnit temp = SpaceUnit.ERROR;
        temp.admin().clear();
        temp.admin().add(client.player.getUuid());
        temp.allowed().clear();
        temp.allowed().add(targetId);
        UnitPayloadC2S.send("request_teleport", temp);
        client.setScreen(null);
    }


    public void refresh(List<SpaceUnit> newUnits) {
        if (!this.units.equals(newUnits)) {
            this.units = newUnits;
            this.clearAndInit();
        }
    }

    @Override
    protected void init() {
        super.init();
        if (units.isEmpty()) {
            switch (page) {
                case "owned" -> fetchOwnedUnits();
                case "admin" -> {}
                case "allowed" -> fetchAllowedUnits();
                case "players" -> units = List.of();
                case "all" -> fetchAllUnits();
            }
        }
        renderPageButtonRow();
        switch (page) {
            case "owned" -> renderEditAbleTypePage(true);
            case "admin" -> {}
            case "allowed" -> renderAllowedUnitPage();
            case "players" -> renderPlayerPage();
            case "all" -> renderAllUnitPage();
        }
    }

    private void deleteUnit(SpaceUnit unit) {
        assert client != null;
        if (client.player != null && unit.owner().equals(client.player.getUuid())) {
            if (client.isInSingleplayer()) {
                SpaceUnitManager.getClientInstance().removeUnit(unit);
                units.remove(unit);
                init();
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