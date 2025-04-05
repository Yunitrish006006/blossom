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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnitList extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private List<SpaceUnit> units = new ArrayList<>();
    public static String page = "owned";
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public UnitList() {
        super(Text.translatable("gui.blossom.teleport.title"));
        fetchUnits(page);
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

    private void addButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction action) {
        this.addDrawableChild(ButtonWidget.builder(text, action).dimensions(x, y, width, height).build());
    }

    private void renderPageButtonRow() {
        int x = this.width / 16;
        int y = 20;
        int buttonWidth = BUTTON_WIDTH/4;
        List<ButtonWidget.PressAction> actions = List.of(
                button -> {
                    page = "owned";
                    fetchUnits(page);
                    clearAndInit();
                },
                button -> {
                    page = "allowed";
                    fetchUnits(page);
                    clearAndInit();
                },
                button -> {
                    page = "players";
                    fetchUnits(page);
                    clearAndInit();
                },
                button -> {
                    page = "all";
                    fetchUnits(page);
                    clearAndInit();
                }
        );
        List<Text> texts = List.of(
                Text.translatable("gui.blossom.teleport.owned_units"),
                Text.translatable("gui.blossom.teleport.allowed_units"),
                Text.translatable("gui.blossom.teleport.players"),
                Text.translatable("gui.blossom.teleport.all_units")
        );

        for (Text text : texts) {
            addButton(x, y, buttonWidth, BUTTON_HEIGHT, text, actions.get(texts.indexOf(text)));
            x += this.width / 16 + buttonWidth;
        }
    }

    private void renderGeneralPage(boolean canEdit) {
        int y = 50;
        if (canEdit) {
            this.addDrawableChild(createNewUnitButton(y));
            y += BUTTON_HEIGHT + 5;
        }

        // 計算可用空間和最大顯示數量
        int availableHeight = this.height - y - 30; // 預留底部空間
        int maxVisibleItems = availableHeight / (BUTTON_HEIGHT + 5);
        int ITEMS_PER_PAGE = Math.min(8, maxVisibleItems); // 動態調整每頁顯示數量

        // 計算最大滾動值
        maxScroll = Math.max(0, units.size() - ITEMS_PER_PAGE);
        scrollOffset = Math.min(scrollOffset, maxScroll); // 確保不會過度滾動

        // 計算滾動按鈕位置
        int contentHeight = ITEMS_PER_PAGE * (BUTTON_HEIGHT + 5);
        int topButtonY = y;
        int bottomButtonY = Math.min(y + contentHeight, height - 30);

        // 只有當有需要滾動時才顯示滾動按鈕
        if (units.size() > ITEMS_PER_PAGE) {
            // 上方滾動按鈕
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("▲"),
                    button -> {
                        scrollOffset = Math.max(0, scrollOffset - 1);
                        clearAndInit();
                    }
            ).dimensions(this.width / 2 + BUTTON_WIDTH / 2 + 5, topButtonY, 20, 20).build());

            // 下方滾動按鈕
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("▼"),
                    button -> {
                        scrollOffset = Math.min(maxScroll, scrollOffset + 1);
                        clearAndInit();
                    }
            ).dimensions(this.width / 2 + BUTTON_WIDTH / 2 + 5, bottomButtonY, 20, 20).build());
        }

        // 渲染可見的傳送點
        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, units.size());

        for (int i = startIndex; i < endIndex; i++) {
            if (y + BUTTON_HEIGHT > this.height - 30) break; // 確保不會超出底部

            SpaceUnit unit = units.get(i);
            this.addDrawableChild(teleportUnitButton(unit, y));
            if (canEdit) {
                this.addDrawableChild(editUnitButton(unit, y));
                this.addDrawableChild(deleteUnitButton(unit, y));
            }
            y += BUTTON_HEIGHT + 5;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            clearAndInit();
            return true;
        } else if (verticalAmount < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
            clearAndInit();
            return true;
        }
        return false;
    }


    private void fetchUnits(String page) {
        if (client==null) return;
        units.clear();
        if (!client.isInSingleplayer()) {
            switch (page) {
                case "owned" -> UnitPayloadC2S.send("fetch_owned", null);
                case "admin", "players" -> {}
                case "allowed" -> UnitPayloadC2S.send("fetch_allowed", null);
                case "all" -> UnitPayloadC2S.send("fetch_all", null);
            }
        }
        else {
            UUID playerId = client.player.getUuid();
            SpaceUnitManager manager = SpaceUnitManager.getClientInstance();
            switch (page) {
                case "owned" -> units = manager.config.getOwned(playerId);
                case "admin", "players" -> {}
                case "allowed" -> units = manager.config.getAllowed(playerId);
                case "all" -> units = manager.config.units;
            }
        }
    }
    /*---------------------------------------player---------------------------------------*/
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
        renderPageButtonRow();
        switch (page) {
            case "owned" -> renderGeneralPage(true);
            case "admin" -> {}
            case "allowed", "all" -> renderGeneralPage(false);
            case "players" -> renderPlayerPage();
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