package net.sigmarik.abilitymod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.sigmarik.abilitymod.command.TraitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
	public static final String TRAIT_ADDICTION = "addiction";

	public static final String TRAIT_FAST = "fast";
	public static final String TRAIT_STRONG = "strong";
	public static final String TRAIT_HATED = "hated";
	public static final String TRAIT_HOT_IRON = "hot_iron";

	public static final int BOAT_ATTRACTION_DISTANCE = 4;

	public static final double BOAT_ATTRACTION_FACTOR = 0.1;

	public static final int FIRE_RESISTANCE_STATUS_EFFECT = 12;

	public static final int FIRING_CHANCE_PER_TICK = 55;

	public static final int ADDICTION_START_TIMER 	= 1800 * 20;	// 30 minutes
	public static final int ADDICTION_MID_TIMER 	= 180 * 20;   	// 3 minutes
	public static final int ADDICTION_WARNING_TIMER = 20 * 20;

	public static final HashSet<Item> IRON_ITEMS = new HashSet<>(List.of(
			Items.ANVIL,
			Items.AXOLOTL_BUCKET,
			Items.BLAST_FURNACE,
			Items.BUCKET,
			Items.CAULDRON,
			Items.CHAIN,
			Items.CHAINMAIL_BOOTS,
			Items.CHAINMAIL_CHESTPLATE,
			Items.CHAINMAIL_HELMET,
			Items.CHAINMAIL_LEGGINGS,
			Items.CHEST_MINECART,
			Items.CHIPPED_ANVIL,
			Items.COMPASS,
			Items.CROSSBOW,
			Items.DAMAGED_ANVIL,
			Items.DEEPSLATE_IRON_ORE,
			Items.DETECTOR_RAIL,
			Items.FLINT_AND_STEEL,
			Items.FURNACE_MINECART,
			Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
			Items.HOPPER,
			Items.HOPPER_MINECART,
			Items.IRON_AXE,
			Items.IRON_BARS,
			Items.IRON_BLOCK,
			Items.IRON_BOOTS,
			Items.IRON_CHESTPLATE,
			Items.IRON_DOOR,
			Items.IRON_HELMET,
			Items.IRON_HOE,
			Items.IRON_HORSE_ARMOR,
			Items.IRON_INGOT,
			Items.IRON_LEGGINGS,
			Items.IRON_NUGGET,
			Items.IRON_ORE,
			Items.IRON_PICKAXE,
			Items.IRON_SHOVEL,
			Items.IRON_SWORD,
			Items.IRON_TRAPDOOR,
			Items.LANTERN,
			Items.LAVA_BUCKET,
			Items.MILK_BUCKET,
			Items.MINECART,
			Items.PISTON,
			Items.POWDER_SNOW_BUCKET,
			Items.RAIL,
			Items.RAW_IRON,
			Items.RAW_IRON_BLOCK,
			Items.SHEARS,
			Items.SHIELD,
			Items.SMITHING_TABLE,
			Items.SOUL_LANTERN,
			Items.STICKY_PISTON,
			Items.STONECUTTER,
			Items.TNT_MINECART,
			Items.TRAPPED_CHEST,
			Items.TRIPWIRE_HOOK,
			Items.WATER_BUCKET
	));

	public static final HashSet<EntityType> NEUTRAL_MOBS = new HashSet<>(List.of(
			EntityType.BEE,
			EntityType.CAVE_SPIDER,
			EntityType.DOLPHIN,
			EntityType.ENDERMAN,
			EntityType.GOAT,
			EntityType.IRON_GOLEM,
			EntityType.LLAMA,
			EntityType.PANDA,
			EntityType.PIGLIN,
			EntityType.POLAR_BEAR,
			EntityType.SPIDER,
			EntityType.WOLF,
			EntityType.ZOMBIFIED_PIGLIN,
			EntityType.TRADER_LLAMA
	));

	public static final double FAST_COEFFICIENT = 0.3;
	public static final UUID FAST_MODIFIER_UUID = new UUID(Random.createLocal().nextLong(), Random.createLocal().nextLong());

	public static final double STRONG_COEFFICIENT = 0.3;
	public static final UUID STRONG_MODIFIER_UUID = new UUID(Random.createLocal().nextLong(), Random.createLocal().nextLong());


	@Override
	public void onInitializeServer() {
		LOGGER.info("Initializing AbilityMod.");

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> TraitCommand.register(dispatcher));
	}
}
