package net.sigmarik.abilitymod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.*;

public class TraitCommand {
    private static final TraitsSuggestionProvider traitSuggestor = new TraitsSuggestionProvider();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("trait").requires(source -> source.hasPermissionLevel(2))
                .then(literal("add")
                        .then(argument("players", EntityArgumentType.players())
                        .then(argument("trait", StringArgumentType.greedyString())
                                .suggests(traitSuggestor)
                                        .executes(TraitCommand::addTrait))))
                .then(literal("remove")
                        .then(argument("players", EntityArgumentType.players())
                        .then(argument("trait", StringArgumentType.greedyString())
                                .suggests(traitSuggestor)
                                        .executes(TraitCommand::removeTrait))))
                .then(literal("list")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(TraitCommand::printTraits)))
        );
    }

    private static int addTrait(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String[] traits = StringArgumentType.getString(context, "trait").split(" ");
        java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> players =
                EntityArgumentType.getPlayers(context, "players");

        ServerState states = ServerState.getTraitStates(context.getSource().getServer());

        for (ServerPlayerEntity player : players) {
            for (String trait : traits) {
                AbilityMod.LOGGER.info("Adding trait " + trait + " to player " + player.getName().getString());
                states.addTrait(player.getUuid(), trait);

                if (trait.equals(AbilityMod.TRAIT_FAST)) {
                    EntityAttributeInstance speed = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    EntityAttributeModifier speedModifier = new EntityAttributeModifier(ServerState.getFastUUID(player),
                            "Fast", AbilityMod.MOVEMENT_SPEED_BASE_MULTIPLY_COEFFICIENT,
                            EntityAttributeModifier.Operation.MULTIPLY_BASE);
                    if (!speed.hasModifier(speedModifier)) {
                        speed.addPersistentModifier(speedModifier);
                    }
                }
                if (trait.equals(AbilityMod.TRAIT_STRONG)) {
                    EntityAttributeInstance strength = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    EntityAttributeModifier strengthModifier = new EntityAttributeModifier(ServerState.getStrongUUID(player),
                            "Strong", AbilityMod.ATTACK_DAMAGE_BASE_MULTIPLY_COEFFICIENT,
                            EntityAttributeModifier.Operation.MULTIPLY_BASE);
                    if (!strength.hasModifier(strengthModifier)) {
                        strength.addPersistentModifier(strengthModifier);
                    }
                }
            }
        }

        states.markDirty();

        return 1;
    }

    private static int removeTrait(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String[] traits = StringArgumentType.getString(context, "trait").split(" ");
        java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> players =
                EntityArgumentType.getPlayers(context, "players");

        ServerState states = ServerState.getTraitStates(context.getSource().getServer());

        boolean allTraits = false;
        for (String trait : traits) {
            allTraits |= trait.equals("*");
        }

        List<String> traitCollection = List.of(traits);

        for (ServerPlayerEntity player : players) {
            if (allTraits) {
                traitCollection = states.getTraitList(player.getUuid()).stream().toList();
            }

            for (String trait : traitCollection) {
                AbilityMod.LOGGER.info("Removing trait " + trait + " from player " + player.getName().getString());
                states.removeTrait(player.getUuid(), trait);

                if (trait.equals(AbilityMod.TRAIT_FAST)) {
                    EntityAttributeInstance speed = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    EntityAttributeModifier speedModifier = new EntityAttributeModifier(ServerState.getFastUUID(player),
                            "Fast", AbilityMod.MOVEMENT_SPEED_BASE_MULTIPLY_COEFFICIENT,
                            EntityAttributeModifier.Operation.MULTIPLY_BASE);
                    if (speed.hasModifier(speedModifier)) {
                        speed.removeModifier(ServerState.getFastUUID(player));
                    }
                }
                if (trait.equals(AbilityMod.TRAIT_STRONG)) {
                    EntityAttributeInstance strength = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    EntityAttributeModifier strengthModifier = new EntityAttributeModifier(ServerState.getStrongUUID(player),
                            "Strong", AbilityMod.ATTACK_DAMAGE_BASE_MULTIPLY_COEFFICIENT,
                            EntityAttributeModifier.Operation.MULTIPLY_BASE);
                    if (strength.hasModifier(strengthModifier)) {
                        strength.removeModifier(ServerState.getStrongUUID(player));
                    }
                }
            }
        }

        states.markDirty();

        return 1;
    }

    private static int printTraits(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

        ServerState states = ServerState.getTraitStates(player.server);
        Set<String> traits = states.getTraitList(player.getUuid());

        if (traits.isEmpty()) {
            player.sendMessage(Text.literal(player.getName().getString() + " has no traits."));
        } else {
            player.sendMessage(Text.literal(player.getName().getString() + "'s traits:"));
            for (String trait : traits) {
                player.sendMessage(Text.literal(trait));
            }
        }

        return 1;
    }

    private static class TraitsSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            builder.suggest(AbilityMod.TRAIT_EASY_PEARLS);
            builder.suggest(AbilityMod.TRAIT_NO_EYE_AGGRO);
            builder.suggest(AbilityMod.TRAIT_FEAR_OF_WATER);
            builder.suggest(AbilityMod.TRAIT_IGNORED_BY_UNDEAD);
            builder.suggest(AbilityMod.TRAIT_DAMAGED_BY_LIGHT);
            builder.suggest(AbilityMod.TRAIT_HARMFUL_ROTTEN_FLESH);
            builder.suggest(AbilityMod.TRAIT_INVERT_EFFECTS);
            builder.suggest(AbilityMod.TRAIT_BOAT_MAGNET);
            builder.suggest(AbilityMod.TRAIT_CLEAN_COSTUME);
            builder.suggest(AbilityMod.TRAIT_ADDICTION);
            builder.suggest(AbilityMod.TRAIT_FAST);
            builder.suggest(AbilityMod.TRAIT_STRONG);
            builder.suggest(AbilityMod.TRAIT_HATED);
            builder.suggest(AbilityMod.TRAIT_HOT_IRON);

            if(context.getNodes().get(1).getNode().getUsageText().equals("remove")) {
                builder.suggest("*");
            }

            return builder.buildFuture();
        }
    }
}
