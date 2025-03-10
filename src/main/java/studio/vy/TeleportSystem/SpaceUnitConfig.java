package studio.vy.TeleportSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpaceUnitConfig {
    public List<SpaceUnit> units = new ArrayList<>();

    public List<SpaceUnit> getAllowed(UUID id) {
        List<SpaceUnit> temp = new ArrayList<>();
        for (SpaceUnit unit : units) {
            if (unit.owner().equals(id) || unit.admin().contains(id) || unit.allowed().contains(id)) {
                temp.add(unit);
            }
        }
        return temp;
    }

    public List<SpaceUnit> getOwned(UUID id) {
        List<SpaceUnit> temp = new ArrayList<>();
        for (SpaceUnit unit : units) {
            if (unit.owner().equals(id)) {
                System.out.println("owned:"+ unit.name());
                temp.add(unit);
            }
            else {
                System.out.println("not owned:"+ unit.name());
            }
        }
        return temp;
    }

    public List<SpaceUnit> getEditable(UUID id) {
        List<SpaceUnit> temp = new ArrayList<>();
        for (SpaceUnit unit : units) {
            if (unit.owner().equals(id) || unit.admin().contains(id)) {
                temp.add(unit);
            }
        }
        return temp;
    }
}
