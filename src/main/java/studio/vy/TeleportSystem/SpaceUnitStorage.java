package studio.vy.TeleportSystem;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class SpaceUnitStorage{

    private static final Path FabricConfigDirection = FabricLoader.getInstance().getConfigDir();
    private final File file;
    private final String fileName = "units.json";
    private SpaceUnitConfig config;

    public SpaceUnitStorage() throws IOException {
        this.file = new File(FabricConfigDirection+"/"+fileName);
        if (file.exists()) {
            config = read();
        }
        else {
            if (file.createNewFile() && file.setReadable(true) && file.setWritable(true)) {
                config = new SpaceUnitConfig();
                write();
            }
        }
    }

    public void clearAndSetUnits(List<SpaceUnit> units) {
        config.units.clear();
        config.units.addAll(units);
        write();
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

    public List<SpaceUnit> getOwnedUnits(UUID uuid) {
        List<SpaceUnit> temp = new java.util.ArrayList<>(List.of());
        for (SpaceUnit unit : config.units) {
            if (unit.owner().equals(uuid)) {
                temp.add(unit);
            }
        }
        return temp;
    }

    public SpaceUnitConfig read() {
        try {
            FileReader reader = new FileReader(file);
            config = new Gson().fromJson(reader, SpaceUnitConfig.class);
//            System.out.println("Reading from file"+file.getPath());
            reader.close();
            return config;
        }
        catch (IOException ignored) {
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

    public void write() {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(beautify(new Gson().toJson(config)));
            writer.close();
        }
        catch (IOException ignored) {
            config = new SpaceUnitConfig();
            write();
        }
    }
}
