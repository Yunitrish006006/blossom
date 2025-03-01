package studio.vy.TeleportSystem;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;

public record SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner, List<UUID> admin,List<UUID> allowed) {

    public SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner) {
        this(name, x, y, z, dimension, owner, List.of(owner), List.of(owner));
    }

    public void teleport(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (owner.equals(id) || admin.contains(id) || allowed.contains(id)) {
            Vec3d pos = player.getPos();
            if (player.teleport(x, y, z, true)) {
                player.sendMessage(Text.of("Traveled for " + Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2) + Math.pow(z - pos.z, 2)) + " blocks"), false);
            } else {
                player.sendMessage(Text.of("Teleport failed - make sure the destination is loaded"), false);
            }
        }
        else {
            player.sendMessage(Text.of("You are not allowed to teleport to this unit"), false);
        }
    }
}
