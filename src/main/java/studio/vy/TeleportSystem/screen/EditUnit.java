package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.SpaceUnit;

public class EditUnit extends Screen {
    private final Screen parent;
    private final SpaceUnit unit;
    private TextFieldWidget playerNameField;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    public EditUnit(Screen parent, SpaceUnit unit) {
        super(Text.translatable("gui.blossom.teleport.edit_permission"));
        this.parent = parent;
        this.unit = unit;
    }

    @Override
    protected void init() {
        super.init();

        // 添加玩家名稱輸入框
        this.playerNameField = new TextFieldWidget(this.textRenderer,
                this.width / 2 - BUTTON_WIDTH / 2,
                50,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.literal(""));
        this.addDrawableChild(this.playerNameField);

        // 添加按鈕
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.add_player"),
                        button -> addPlayer())
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, 80, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build()
        );

        // 返回按鈕
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.back"),
                        button -> this.close())
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, this.height - 40, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build()
        );
    }

    private void addPlayer() {
        String playerName = this.playerNameField.getText();
        if (!playerName.isEmpty()) {
            ServerPlayerEntity target = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(playerName);
            if (target != null) {
                unit.allowed().add(target.getUuid());
                UnitPayloadC2S.send("add_allowed", unit);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
