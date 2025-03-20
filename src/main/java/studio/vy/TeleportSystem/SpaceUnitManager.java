package studio.vy.TeleportSystem;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import studio.vy.TeleportSystem.Component.SpaceUnit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpaceUnitManager {
    private static SpaceUnitManager serverInstance;
    private static SpaceUnitManager clientInstance;
    private final File file;
    public SpaceUnitConfig config;

    private SpaceUnitManager(MinecraftServer server, boolean isClient) {
        if (isClient) {
            this.file = new File(FabricLoader.getInstance().getGameDir() + "/Blossom/Units.json");
        } else {
            this.file = new File(FabricLoader.getInstance().getConfigDir() + "/Blossom/ServerUnits.json");
        }
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        if (file.exists()) {
            config = read();
        } else {
            try {
                file.createNewFile();
                config = new SpaceUnitConfig();
                write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<SpaceUnit> getAllUnits() {
        return new ArrayList<>(config.units);
    }

    public static SpaceUnitManager getServerInstance(MinecraftServer server) {
        if (serverInstance == null) {
            serverInstance = new SpaceUnitManager(server, false);
        }
        return serverInstance;
    }

    public static SpaceUnitManager getClientInstance(MinecraftServer server) {
        if (clientInstance == null) {
            clientInstance = new SpaceUnitManager(server, true);
        }
        return clientInstance;
    }

    public void addUnit(SpaceUnit unit) {
        config.units.add(unit);
        write();
    }

    public void removeUnit(SpaceUnit unit) {
        config.units.remove(unit);
        write();
    }

    private SpaceUnitConfig read() {
        try {
            FileReader reader = new FileReader(file);
            config = new Gson().fromJson(reader, SpaceUnitConfig.class);
            reader.close();
            return config;
        } catch (IOException e) {
            return new SpaceUnitConfig();
        }
    }

    public String beautify(String string) {
        StringBuilder temp = new StringBuilder();
        int tabCounter = 0;
        char[] array = string.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char it = array[i];
            if (it == '{' || it == '[') {
                tabCounter += 1;
                temp.append(it).append("\n");
                if (tabCounter > 0) temp.append("\t".repeat(tabCounter));
            } else if (it == ',') {
                temp.append(it).append("\n");
                if (tabCounter > 0) temp.append("\t".repeat(tabCounter));
            } else {
                temp.append(it);
            }
            if (i + 1 < array.length) {
                if (array[i + 1] == '}' || array[i + 1] == ']') {
                    temp.append("\n");
                    tabCounter -= 1;
                    if (tabCounter > 0) temp.append("\t".repeat(tabCounter));
                }
            }
        }
        return temp.toString();
    }

    private void write() {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(beautify(new Gson().toJson(config)));
            writer.close();
        } catch (IOException e) {
            config = new SpaceUnitConfig();
            write();
        }
    }

    public void addAllowedPlayer(SpaceUnit unit, UUID playerUuid) {
        config.units.stream()
                .filter(u -> u.equals(unit))
                .findFirst()
                .ifPresent(u -> {
                    List<UUID> newAllowed = new ArrayList<>(u.allowed());
                    if (!newAllowed.contains(playerUuid)) {
                        newAllowed.add(playerUuid);
                        config.units.remove(u);
                        config.units.add(new SpaceUnit(
                                u.name(), u.x(), u.y(), u.z(),
                                u.dimension(), u.owner(), u.admin(), newAllowed
                        ));
                        write();
                    }
                });
    }
}
