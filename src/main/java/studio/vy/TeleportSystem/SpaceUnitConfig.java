package studio.vy.TeleportSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpaceUnitConfig {
    public List<SpaceUnit> units = new ArrayList<>();

    public List<SpaceUnit> getAllowed(UUID id) {
        return units.stream().filter(unit ->
                unit.owner().equals(id) ||
                unit.admin().contains(id) ||
                unit.allowed().contains(id)
        ).collect(Collectors.toList());
    }

    public List<SpaceUnit> getOwned(UUID id) {
        return units.stream().filter(unit -> unit.owner().equals(id)).collect(Collectors.toList());
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
