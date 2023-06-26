package net.sigmarik.abilitymod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.sigmarik.abilitymod.command.TraitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbilityMod implements DedicatedServerModInitializer {
	public static final String MOD_ID = "abilitymod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String TRAIT_EASY_PEARLS = "easy_pearls";
	public static final String TRAIT_NO_EYE_AGGRO = "no_eye_aggro";
	public static final String TRAIT_FEAR_OF_WATER = "fear_of_water";

	public static final String TRAIT_IGNORED_BY_UNDEAD = "ignored_by_undead";
	public static final String TRAIT_DAMAGED_BY_LIGHT = "damaged_by_light";
	public static final String TRAIT_HARMFUL_ROTTEN_FLESH = "harmful_rotten_flesh";
	public static final String TRAIT_INVERT_EFFECTS = "invert_effects";

	public static final String TRAIT_BOAT_MAGNET = "boat_magnet";
	public static final String TRAIT_CLEAN_COSTUME = "clean_costume";
	public static final String TRAIT_QING = "qing";

	public static final String TRAIT_FAST = "fast";
	public static final String TRAIT_STRONG = "strong";
	public static final String TRAIT_HATED = "hated";
	public static final String TRAIT_HOT_IRON = "hot_iron";

	@Override
	public void onInitializeServer() {
		LOGGER.info("Initializing AbilityMod.");

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> TraitCommand.register(dispatcher));
	}
}
