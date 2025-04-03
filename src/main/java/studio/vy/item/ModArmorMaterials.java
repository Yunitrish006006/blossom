package studio.vy.item;

import com.google.common.collect.Maps;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import studio.vy.Blossom;
import studio.vy.util.ModTags;

import java.util.Map;

public class ModArmorMaterials {

    public static final ArmorMaterial COPPER = new ArmorMaterial(500, createDefenseMap(2,4,6,2,4), 20, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0, 0, ModTags.Items.COPPER_REPAIR,
            RegistryKey.of(RegistryKey.ofRegistry(Identifier.ofVanilla("equipment_asset")), Blossom.identifier("copper")));

    private static Map<EquipmentType, Integer> createDefenseMap(int bootsDefense, int leggingsDefense, int chestplateDefense, int helmetDefense, int bodyDefense) {
        return Maps.newEnumMap(Map.of(EquipmentType.BOOTS, bootsDefense, EquipmentType.LEGGINGS, leggingsDefense, EquipmentType.CHESTPLATE, chestplateDefense, EquipmentType.HELMET, helmetDefense, EquipmentType.BODY, bodyDefense));
    }
}


