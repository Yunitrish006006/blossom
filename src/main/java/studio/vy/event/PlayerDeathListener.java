package studio.vy.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import studio.vy.TeleportSystem.Component.SpaceUnit;
import studio.vy.TeleportSystem.SpaceUnitManager;

public class PlayerDeathListener {
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            SpaceUnit newUnit = new SpaceUnit(player.getName().getString()+"'s grave", player);
            SpaceUnitManager.getServerInstance().addUnit(newUnit);
            return true;
        });
    }
}
