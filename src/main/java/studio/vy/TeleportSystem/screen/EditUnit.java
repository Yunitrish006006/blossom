package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.SpaceUnitManager;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.Component.SpaceUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EditUnit extends Screen {
    private final Screen parent;
    private final SpaceUnit unit;
    private final List<UUID> onlinePlayers = new ArrayList<>();
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PLAYER_BUTTON_HEIGHT = 32;
    private static final int PLAYERS_PER_PAGE = 5;
    private String currentTab = "all"; // "all", "admin", "allowed", "owner"

    public EditUnit(Screen parent, SpaceUnit unit) {
        super(Text.translatable("gui.blossom.teleport.edit_permission"));
        this.parent = parent;
        this.unit = unit;
        updateOnlinePlayers();
    }

    private void updateOnlinePlayers() {
        onlinePlayers.clear();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                UUID playerId = entry.getProfile().getId();
                // 排除自己和已有權限的玩家
                if (!playerId.equals(client.player.getUuid())
                        && !unit.allowed().contains(playerId)
                        && !unit.admin().contains(playerId)
                        && !unit.owner().equals(playerId)) {
                    onlinePlayers.add(playerId);
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int tabWidth = BUTTON_WIDTH / 4;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All"),
                button -> {
                    currentTab = "all";
                    updateOnlinePlayers();
                    clearAndInit();
                }
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2, 10, tabWidth, BUTTON_HEIGHT).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Admin"),
                button -> {
                    currentTab = "admin";
                    updateOnlinePlayers();
                    clearAndInit();
                }
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2 + tabWidth, 10, tabWidth, BUTTON_HEIGHT).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Allowed"),
                button -> {
                    currentTab = "allowed";
                    updateOnlinePlayers();
                    clearAndInit();
                }
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2 + tabWidth * 2, 10, tabWidth, BUTTON_HEIGHT).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Owner"),
                button -> {
                    currentTab = "owner";
                    updateOnlinePlayers();
                    clearAndInit();
                }
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2 + tabWidth * 3, 10, tabWidth, BUTTON_HEIGHT).build());

        int startY = 80;
        List<UUID> displayList;
        switch (currentTab) {
            case "admin" -> displayList = new ArrayList<>(unit.admin());
            case "allowed" -> displayList = new ArrayList<>(unit.allowed());
            case "owner" -> displayList = Collections.singletonList(unit.owner());
            default -> displayList = onlinePlayers;
        }

        int scrollOffset = 0;
        for (int i = 0; i < Math.min(PLAYERS_PER_PAGE, displayList.size() - scrollOffset); i++) {
            UUID playerId = displayList.get(scrollOffset + i);
            final String playerName = getPlayerName(playerId);

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(playerName),
                    button -> handlePlayerClick(playerId)
            ).dimensions(
                    this.width / 2 - BUTTON_WIDTH / 2,
                    startY + (i * (PLAYER_BUTTON_HEIGHT + 5)),
                    BUTTON_WIDTH - 30,
                    PLAYER_BUTTON_HEIGHT
            ).build());

            if (!currentTab.equals("owner")) {
                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("×"),
                        button -> removePlayer(playerId)
                ).dimensions(
                        this.width / 2 + BUTTON_WIDTH / 2 - 25,
                        startY + (i * (PLAYER_BUTTON_HEIGHT + 5)),
                        20,
                        PLAYER_BUTTON_HEIGHT
                ).build());
            }
        }

        // 返回按鈕
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.blossom.teleport.back"),
                button -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2, this.height - 40, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private String getPlayerName(UUID playerId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().getId().equals(playerId)) {
                    return entry.getProfile().getName();
                }
            }
        }
        return playerId.toString().substring(0, 8);
    }

    private void handlePlayerClick(UUID playerId) {
        if (currentTab.equals("all")) {
            if (MinecraftClient.getInstance().isInSingleplayer()) {
                unit.allowed().add(playerId);
                SpaceUnitManager.getClientInstance().addAllowedPlayer(unit, playerId);
                MinecraftClient.getInstance().setScreen(parent);
            } else {
                // 建立一個臨時的 SpaceUnit，只保留必要資訊
                SpaceUnit tempUnit = new SpaceUnit(
                        unit.name(),
                        unit.x(),
                        unit.y(),
                        unit.z(),
                        unit.dimension(),
                        unit.owner()
                );
                // 清空預設的權限列表
                tempUnit.allowed().clear();
                // 只添加目標玩家
                tempUnit.allowed().add(playerId);
                UnitPayloadC2S.send("add_allowed", tempUnit);
                MinecraftClient.getInstance().setScreen(parent);
            }
        }
    }

    private void removePlayer(UUID playerId) {
        switch (currentTab) {
            case "admin" -> unit.admin().remove(playerId);
            case "allowed" -> unit.allowed().remove(playerId);
        }
        if (!MinecraftClient.getInstance().isInSingleplayer()) {
            UnitPayloadC2S.send("update_permissions", unit);
        }
        updateOnlinePlayers();
        clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}