package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;
import studio.vy.TeleportSystem.Component.SpaceUnit;
import studio.vy.TeleportSystem.SpaceUnitManager;

public class CreateUnit extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;

    public CreateUnit(Screen parent) {
        super(Text.translatable("gui.blossom.teleport.create_new_unit"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Name input field
        nameField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - FIELD_WIDTH / 2,
                50,
                FIELD_WIDTH,
                FIELD_HEIGHT,
                Text.translatable("gui.blossom.teleport.name")
        );
        nameField.setMaxLength(32);
        this.addDrawableChild(nameField);

        // Create button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.create"),
                        button -> {
                            if (!nameField.getText().isEmpty()) {
                                createUnit(nameField.getText());
                            }
                        })
                .dimensions(this.width / 2 - FIELD_WIDTH / 2, 80, FIELD_WIDTH, FIELD_HEIGHT)
                .build()
        );

        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.blossom.teleport.cancel"),
                        button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(this.width / 2 - FIELD_WIDTH / 2, 110, FIELD_WIDTH, FIELD_HEIGHT)
                .build()
        );
    }

    private void createUnit(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            double x = client.player.getX();
            double y = client.player.getY();
            double z = client.player.getZ();
            String dimension = client.player.getWorld().getRegistryKey().getValue().toString();
            SpaceUnit unit = new SpaceUnit(name, x, y, z, dimension, client.player.getUuid());

            if (client.isInSingleplayer()) {
                SpaceUnitManager.getClientInstance().addUnit(unit);
            } else {
                UnitPayloadC2S.send("add", unit);
                UnitPayloadC2S.send("fetch_owned", null);
            }
            client.setScreen(parent);
        }
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context,mouseX,mouseY,delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}