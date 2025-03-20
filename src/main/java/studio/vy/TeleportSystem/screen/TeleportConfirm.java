package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.Component.SpaceUnit;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;

import java.util.UUID;

public class TeleportConfirm extends Screen {
    private final UUID requester;
    private final String requesterName;
    private final ServerPlayerEntity requesterPlayer;
    private final ServerPlayerEntity targetPlayer;

    public TeleportConfirm(ServerPlayerEntity requester, ServerPlayerEntity target) {
        super(Text.translatable("gui.blossom.teleport.request"));
        this.requester = requester.getUuid();
        this.requesterName = requester.getNameForScoreboard();
        this.requesterPlayer = requester;
        this.targetPlayer = target;
    }

    @Override
    protected void init() {
        int y = height / 2;

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.blossom.teleport.accept"),
                button -> {
                    acceptTeleport();
                    close();
                }
        ).dimensions(width/2 - 105, y, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.blossom.teleport.reject"),
                button -> {
                    rejectTeleport();
                    close();
                }
        ).dimensions(width/2 + 5, y, 100, 20).build());
    }

    private void acceptTeleport() {
        SpaceUnit tempUnit = new SpaceUnit(
                UUID.randomUUID().toString(),
                targetPlayer.getPos(),
                targetPlayer.getWorld().getRegistryKey().getValue().toString(),
                targetPlayer.getUuid()
                );

        // 添加請求者為允許傳送的玩家
        tempUnit.allowed().add(requester);

        // 發送到服務器創建並傳送
        UnitPayloadC2S.send("temp_teleport", tempUnit);
    }

    private void rejectTeleport() {
        if (!requesterPlayer.isDisconnected()) {
            requesterPlayer.sendMessage(
                    Text.translatable("gui.blossom.teleport.rejected", targetPlayer.getNameForScoreboard()),
                    false
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("gui.blossom.teleport.request.message", requesterName).getString(),
                width/2, height/2 - 30, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

}