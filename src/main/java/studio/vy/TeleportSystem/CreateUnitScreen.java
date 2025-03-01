package studio.vy.TeleportSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.IOException;

public class CreateUnitScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    public static SpaceUnitStorage storage;

    static {
        try {
            storage = new SpaceUnitStorage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CreateUnitScreen(Screen parent) {
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
                Text.translatable("Name")
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
//            if (client.isInSingleplayer()) {
                storage.addUnit(new SpaceUnit(name, x, y, z, dimension, client.player.getUuid()));
//            }
//            else {
//                sendCreateUnitPacket(name, x, y, z, dimension);
//            }
            client.setScreen(parent);
        }
    }

    private void sendCreateUnitPacket(String name, double x, double y, double z, String dimension) {

    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context,mouseX,mouseY,delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}