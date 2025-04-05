package studio.vy.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import studio.vy.TeleportSystem.Component.SpaceUnit;
import studio.vy.TeleportSystem.SpaceUnitManager;

public class PlayerDeathListener {
    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                SpaceUnit newUnit = new SpaceUnit(oldPlayer.getName().getString()+"'s grave",oldPlayer);
                SpaceUnitManager.getServerInstance().addUnit(newUnit);
            }
        });
    }
}
