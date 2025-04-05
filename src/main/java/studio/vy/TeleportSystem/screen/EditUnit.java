package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.server.network.ServerPlayerEntity;
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
    private final List<ServerPlayerEntity> onlinePlayers = new ArrayList<>();
    private int scrollOffset = 0;
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

        // 顯示玩家列表
        int startY = 80;
        List<UUID> displayList = switch (currentTab) {
            case "admin" -> unit.admin();
            case "allowed" -> unit.allowed();
            case "owner" -> Collections.singletonList(unit.owner());
            default -> onlinePlayers.stream().map(ServerPlayerEntity::getUuid).toList();
        };

        for (int i = 0; i < Math.min(PLAYERS_PER_PAGE, displayList.size() - scrollOffset); i++) {
            UUID playerId = displayList.get(scrollOffset + i);
            String playerName = getPlayerName(playerId);

            // 新增玩家按鈕
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(playerName),
                    button -> handlePlayerClick(playerId)
            ).dimensions(this.width / 2 - BUTTON_WIDTH / 2, startY + (i * (PLAYER_BUTTON_HEIGHT + 5)),
                    BUTTON_WIDTH - 30, PLAYER_BUTTON_HEIGHT).build());

            // 如果不是 owner 頁面，添加刪除按鈕
            if (!currentTab.equals("owner")) {
                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("×"),
                        button -> removePlayer(playerId)
                ).dimensions(this.width / 2 + BUTTON_WIDTH / 2 - 25,
                        startY + (i * (PLAYER_BUTTON_HEIGHT + 5)), 20, PLAYER_BUTTON_HEIGHT).build());
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
            // 添加到 allowed 列表
            unit.allowed().add(playerId);
            if (MinecraftClient.getInstance().isInSingleplayer()) {
                SpaceUnitManager.getClientInstance().addAllowedPlayer(unit, playerId);
            } else {
                UnitPayloadC2S.send("add_allowed", unit);  // 確保伺服器端有對應的處理邏輯
            }
            updateOnlinePlayers();  // 更新完後重新整理列表
            clearAndInit();
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

    private void updateOnlinePlayers() {
        onlinePlayers.clear();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().getPlayerList().forEach(entry -> {
                ServerPlayerEntity player = client.getServer() != null ?
                        client.getServer().getPlayerManager().getPlayer(entry.getProfile().getId()) : null;
                if (player != null && !unit.allowed().contains(player.getUuid())
                        && !unit.admin().contains(player.getUuid())) {
                    onlinePlayers.add(player);
                }
            });
        }
    }

    private void filterPlayers(String search) {
        if (!search.isEmpty()) {
            onlinePlayers.removeIf(player ->
                    !player.getNameForScoreboard().toLowerCase().contains(search.toLowerCase())
            );
        }
        scrollOffset = 0;
        clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}