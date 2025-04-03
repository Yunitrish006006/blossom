package studio.vy.item;

import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import studio.vy.Blossom;

public interface ModEquipmentAssetKeys {
    RegistryKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY = RegistryKey.ofRegistry(Blossom.identifier("equipment_asset"));

    RegistryKey<EquipmentAsset> COPPER = register("copper");

    static RegistryKey<EquipmentAsset> register(String name) {
        return RegistryKey.of(REGISTRY_KEY, Blossom.identifier(name));
    }
}
