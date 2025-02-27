package studio.vy.TeleportSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UnitManager {
    private static final String FILENAME = "space_units.json";
    private final File configDir;
    private List<SpaceUnit> spaceUnits = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public UnitManager(MinecraftServer server) {
        this.configDir = server.getRunDirectory().toFile();
        loadSpaceUnits();
    }

    public void addSpaceUnit(SpaceUnit unit) {
        spaceUnits.add(unit);
        saveSpaceUnits();
    }

    public List<SpaceUnit> getSpaceUnits() {
        return spaceUnits;
    }

    private void loadSpaceUnits() {
        File file = new File(configDir, FILENAME);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<List<SpaceUnit>>(){}.getType();
                spaceUnits = gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSpaceUnits() {
        File file = new File(configDir, FILENAME);
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(spaceUnits, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
