package net.nukebob.nbconfig;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;
import net.nukebob.nbconfig.config.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NukebobConfig implements ClientModInitializer {
	public static final String MOD_ID = "nbconfig";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("NukebobConfig loaded");
		MainConfig.loadConfig();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
