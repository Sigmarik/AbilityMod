package net.sigmarik.abilitymod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.sigmarik.abilitymod.command.TraitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbilityMod implements DedicatedServerModInitializer {
	public static final String MOD_ID = "abilitymod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeServer() {
		LOGGER.info("Initializing AbilityMod.");

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> TraitCommand.register(dispatcher));
	}
}
