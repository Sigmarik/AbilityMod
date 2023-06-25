package net.sigmarik.abilitymod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.sigmarik.abilitymod.command.TraitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbilityMod implements DedicatedServerModInitializer {
	public static final String MOD_ID = "abilitymod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String TRAIT_EASY_PEARLS = "easy_pearls";
	public static final String TRAIT_NO_EYE_AGGRO = "no_eye_aggro";
	public static final String TRAIT_FEAR_OF_WATER = "fear_of_water";

	@Override
	public void onInitializeServer() {
		LOGGER.info("Initializing AbilityMod.");

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> TraitCommand.register(dispatcher));
	}
}
