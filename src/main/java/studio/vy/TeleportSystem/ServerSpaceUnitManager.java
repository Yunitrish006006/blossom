package studio.vy.TeleportSystem;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ServerSpaceUnitManager {
    private static ServerSpaceUnitManager instance;
    private final File file;
    private SpaceUnitConfig config;

    private ServerSpaceUnitManager(MinecraftServer server) {
        this.file = new File(FabricLoader.getInstance().getConfigDir()+"/ServerUnits.json");
        System.out.println(file.getAbsolutePath());
        if (file.exists()) {
            config = read();
        } else {
            try {
                file.getParentFile().mkdirs();
                if (file.createNewFile()) {
                    config = new SpaceUnitConfig();
                    write();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ServerSpaceUnitManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new ServerSpaceUnitManager(server);
        }
        return instance;
    }

    public List<SpaceUnit> getVisibleUnits(UUID playerUuid) {
        return config.units.stream()
                .filter(unit -> unit.owner().equals(playerUuid) ||
                        unit.admin().contains(playerUuid) ||
                        unit.allowed().contains(playerUuid))
                .collect(java.util.stream.Collectors.toList());
    }

    public void addUnit(SpaceUnit unit) {
        config.units.add(unit);
        write();
    }

    public void removeUnit(SpaceUnit unit) {
        config.units.remove(unit);
        write();
    }

    public List<SpaceUnit> getAllUnits() {
        return config.units;
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
}
