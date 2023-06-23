package net.sigmarik.abilitymod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sigmarik.abilitymod.AbilityMod;

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
                                .executes(TraitCommand::removeTrait)))
                )
        );
    }

    private static int addTrait(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String trait = StringArgumentType.getString(context, "trait");
        java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> players =
                EntityArgumentType.getPlayers(context, "players");
        for (ServerPlayerEntity player : players) {
            AbilityMod.LOGGER.info("Adding trait " + trait + " to player " + player.getName().getString());
        }
        return 1;
    }

    private static int removeTrait(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String trait = StringArgumentType.getString(context, "trait");
        java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> players =
                EntityArgumentType.getPlayers(context, "players");
        for (ServerPlayerEntity player : players) {
            AbilityMod.LOGGER.info("Removing trait " + trait + " from player " + player.getName().getString());
        }
        return 1;
    }
}
