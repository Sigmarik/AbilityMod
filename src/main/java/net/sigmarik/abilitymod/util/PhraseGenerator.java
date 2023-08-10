package net.sigmarik.abilitymod.util;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.random.RandomGenerator;

public class PhraseGenerator {
    public static final String[] FRAMES = {
            "~~~~~~~~~~~~~~~", "===============", "---------------", "***************"
    };
    public static final String[] HEADERS = {
            "Here lies", "Rest in piece", "Rest in pieces", "Here rests", "Final stand of", "R.I.P.", "The remains of",
            "The regrets of"
    };
    public static final String[] PREFIXES = {
            "He was a", "She was a", "We honor our", "We disgrace our", "the"
    };
    public static final String[] OBJECTIVES = {
            "funny", "false", "true", "disposable", "heroic", "brave", "foolish", "smart", "generic",
            "immortal", "honorable", "silent", "loud", "freakish", "fragile", "giga", "stupid", "sore",
            "underestimated", "spooky", "active", "passive", "honest", "simple", "sober", "drunk", "praying",
            "explosive", "promising", "disappointing", "beautiful", "handsome", "ugly", "thin", "fat", "tall", "short",
            "unknown", "famous", "criminal", "white", "black", "yellow", "red", "blue", "hurting", "healing",
            "fearless", "careless", "ghostly", "real", "disgusting", "french"
    };
    public static final String[] NOUNS = {
            "man", "woman", "boy", "girl", "elder", "hero", "chad", "fool", "fighter", "explorer", "journalist",
            "scout", "soldier", "builder", "farmer", "ghost", "human", "mob", "animal", "beast", "medic", "monster",
            "engineer", "miner", "worker", "defender", "bot", "leaflover", "elf", "mudminer", "dwarf", "sailor", "bug",
            "admiral", "captain", "archer", "destroyer", "griefer", "admin", "player", "hunter", "prey", "postman",
            "ghost hunter", "hobbit", "wizard", "creator", "thinker", "philosopher", "professor", "teacher", "knight",
            "liar", "peasant", "entity", "developer", "comedian", "showman", "snowman", "traveller", "scum", "spy",
            "scientist", "mercenary", "lady", "gentleman", "french"
    };

    public static void generateGraveSign(SignBlockEntity topSign, SignBlockEntity bottomSign, String name, Random random) {
        topSign.setTextOnRow(0, Text.literal(FRAMES     [random.nextInt(FRAMES.length)]));
        topSign.setTextOnRow(1, Text.literal(HEADERS    [random.nextInt(HEADERS.length)]));
        topSign.setTextOnRow(2, Text.literal(name));
        bottomSign.setTextOnRow(0, Text.literal(PREFIXES    [random.nextInt(PREFIXES.length)]));
        bottomSign.setTextOnRow(1, Text.literal(OBJECTIVES  [random.nextInt(OBJECTIVES.length)]));
        bottomSign.setTextOnRow(2, Text.literal(NOUNS       [random.nextInt(NOUNS.length)]));
        bottomSign.setTextOnRow(3, Text.literal(FRAMES      [random.nextInt(FRAMES.length)]));
    }
}
