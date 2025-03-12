package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.PlayerHeadTexture;
import studio.vy.TeleportSystem.SpaceUnitManager;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.SpaceUnit;

import java.util.ArrayList;
import java.util.List;

public class EditUnit extends Screen {
    private final Screen parent;
    private final SpaceUnit unit;
    private TextFieldWidget searchField;
    private final List<ServerPlayerEntity> onlinePlayers = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PLAYER_BUTTON_HEIGHT = 32;
    private static final int PLAYERS_PER_PAGE = 5;

    public EditUnit(Screen parent, SpaceUnit unit) {
        super(Text.translatable("gui.blossom.teleport.edit_permission"));
        this.parent = parent;
        this.unit = unit;
        updateOnlinePlayers();
    }

    private void updateOnlinePlayers() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            onlinePlayers.clear();
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (!unit.allowed().contains(player.getUuid())) {
                    onlinePlayers.add(player);
                }
            });
        }
    }

    @Override
    protected void init() {
        super.init();

        this.searchField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - BUTTON_WIDTH / 2,
                20,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.translatable("gui.blossom.teleport.search_player")
        );
        this.searchField.setChangedListener(text -> {
            updateOnlinePlayers();
            filterPlayers(text);
        });
        this.addDrawableChild(this.searchField);

        int startY = 60;
        for (int i = 0; i < Math.min(PLAYERS_PER_PAGE, onlinePlayers.size() - scrollOffset); i++) {
            ServerPlayerEntity player = onlinePlayers.get(scrollOffset + i);
            this.addDrawableChild(createPlayerButton(player, startY + (i * (PLAYER_BUTTON_HEIGHT + 5))));
        }

        // 翻頁按鈕
        if (scrollOffset > 0) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("↑"),
                    button -> {
                        scrollOffset = Math.max(0, scrollOffset - PLAYERS_PER_PAGE);
                        clearAndInit();
                    }
            ).dimensions(this.width / 2 - 10, startY - 30, 20, 20).build());
        }

        if (scrollOffset + PLAYERS_PER_PAGE < onlinePlayers.size()) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("↓"),
                    button -> {
                        scrollOffset = Math.min(onlinePlayers.size() - 1, scrollOffset + PLAYERS_PER_PAGE);
                        clearAndInit();
                    }
            ).dimensions(this.width / 2 - 10, startY + (PLAYER_BUTTON_HEIGHT + 5) * PLAYERS_PER_PAGE, 20, 20).build());
        }

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.blossom.teleport.back"),
                button -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2, this.height - 40, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private ButtonWidget createPlayerButton(ServerPlayerEntity player, int y) {
        return ButtonWidget.builder(
                Text.literal(player.getNameForScoreboard()),
                button -> {
                    if (MinecraftClient.getInstance().isInSingleplayer()) {
                        SpaceUnitManager.getClientInstance(MinecraftClient.getInstance().getServer())
                                .addAllowedPlayer(unit, player.getUuid());
                    } else {
                        UnitPayloadC2S.send("add_allowed", unit);
                    }
                    updateOnlinePlayers();
                    clearAndInit();
                }
        ).dimensions(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, PLAYER_BUTTON_HEIGHT).build();
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

        // 繪製玩家頭像
        int startY = 60;
        for (int i = 0; i < Math.min(PLAYERS_PER_PAGE, onlinePlayers.size() - scrollOffset); i++) {
            ServerPlayerEntity player = onlinePlayers.get(scrollOffset + i);
            int y = startY + (i * (PLAYER_BUTTON_HEIGHT + 5));
//            PlayerHeadTexture playerHeadTexture = new PlayerHeadTexture(player.getUuid());
//            context.drawTexture(
//                    playerHeadTexture.getTexture(),
//                    this.width / 2 - BUTTON_WIDTH / 2 + 5,
//                    y + 2,
//                    32, 32,
//                    8, 8,
//                    8, 8,
//                    64, 64
//            );
        }

        super.render(context, mouseX, mouseY, delta);
    }
}