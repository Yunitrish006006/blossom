package studio.vy;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.vy.block.ModBlocks;
import studio.vy.item.ModItemGroups;
import studio.vy.item.ModItems;

public class Blossom implements ModInitializer {
	public static final String MOD_ID = "blossom";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier identifier(String name) {
		return Identifier.of(MOD_ID, name);
	}
	@Override
	public void onInitialize() {
		ModPayload.initCommon();
		ModPayload.initServer();
		LOGGER.info("initializing mod");
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModItemGroups.registerModItemGroups();
		}
}