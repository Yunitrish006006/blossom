package studio.vy.TeleportSystem.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import studio.vy.TeleportSystem.Component.SpaceUnit;
import studio.vy.TeleportSystem.payload.UnitPayloadC2S;

import java.util.UUID;

public class TeleportConfirm extends Screen {
    private final UUID requester;
    private final UUID target;

    public TeleportConfirm(UUID requester, UUID target) {
        super(Text.translatable("gui.blossom.teleport.request"));
        this.requester = requester;
        this.target = target;
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
        SpaceUnit tempUnit = SpaceUnit.ERROR;
        tempUnit.admin().clear();
        tempUnit.admin().add(requester);
        tempUnit.allowed().clear();
        tempUnit.allowed().add(target);

        System.out.println("accept==============================");
        System.out.println("request:"+requester);
        System.out.println("target:"+target);
        System.out.println("====================================");

        UnitPayloadC2S.send("player_teleport", tempUnit);
    }

    private void rejectTeleport() {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        String requestPlayerName = "";
        for (PlayerListEntry request : client.getNetworkHandler().getPlayerList()) {
            if (request.getProfile().getId().equals(requester)) {
                requestPlayerName = request.getProfile().getName();
            }
        }
        context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("gui.blossom.teleport.request.message", requestPlayerName).getString(),
                width/2, height/2 - 30, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}