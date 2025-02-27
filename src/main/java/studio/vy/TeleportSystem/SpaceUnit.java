package studio.vy.TeleportSystem;

import java.util.UUID;

public record SpaceUnit(String name, double x, double y, double z, String dimension, UUID owner) {
}
