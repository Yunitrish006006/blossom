package studio.vy.TeleportSystem;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import studio.vy.TeleportSystem.Component.SpaceUnit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class SpaceUnitManager {
    private static SpaceUnitManager serverInstance;
    private static SpaceUnitManager clientInstance;
    private final File file;
    public SpaceUnitConfig config;

    private SpaceUnitManager(boolean isClient) {
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
            } catch (IOException ignored) {}
        }
    }

    public static SpaceUnitManager getServerInstance() {
        if (serverInstance == null) {
            serverInstance = new SpaceUnitManager(false);
        }
        return serverInstance;
    }

    public static SpaceUnitManager getClientInstance() {
        if (clientInstance == null) {
            clientInstance = new SpaceUnitManager(true);
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
                    if (!u.allowed().contains(playerUuid)) {
                        u.allowed().add(playerUuid);
                    }
                });
        write();
    }
}
