package net.openbagtwo.foxnap;

import net.fabricmc.api.ModInitializer;
import net.openbagtwo.foxnap.discs.DiscRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoxNap implements ModInitializer {

	public static final String MOD_ID = "foxnap";
	public static final String MOD_NAME = "Fox Nap";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Registering FoxNap Music Library");
		DiscRegistry.init();
		LOGGER.info("FoxNap Initialization Complete");
	}
}
