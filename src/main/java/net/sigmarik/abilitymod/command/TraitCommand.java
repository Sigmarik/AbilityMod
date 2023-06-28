package net.sigmarik.abilitymod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.*;

public class TraitCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("trait").requires(source -> source.hasPermissionLevel(2))
                .then(literal("add")
                        .then(argument("players", EntityArgumentType.players())
                        .then(argument("trait", StringArgumentType.word()).suggests(SuggestionProviders.ALL_RECIPES)
                        .executes(TraitCommand::addTrait))))
                .then(literal("remove")
                        .then(argument("players", EntityArgumentType.players())
                        .then(argument("trait", StringArgumentType.word())
                                .executes(TraitCommand::removeTrait))))
                .then(literal("list")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(TraitCommand::printTraits)))
        );
    }

    private static int addTrait(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String trait = StringArgumentType.getString(context, "trait");
        java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> players =
                EntityArgumentType.getPlayers(context, "players");

        ServerState states = ServerState.getTraitStates(context.getSource().getServer());

        for (ServerPlayerEntity player : players) {
            AbilityMod.LOGGER.info("Adding trait " + trait + " to player " + player.getName().getString());

            states.addTrait(player.getUuid(), trait);
        }

        states.markDirty();

        return 1;
    }

    private static int removeTrait(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String trait = StringArgumentType.getString(context, "trait");
        java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> players =
                EntityArgumentType.getPlayers(context, "players");

        ServerState states = ServerState.getTraitStates(context.getSource().getServer());

        for (ServerPlayerEntity player : players) {
            AbilityMod.LOGGER.info("Removing trait " + trait + " from player " + player.getName().getString());

            states.removeTrait(player.getUuid(), trait);
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
}
