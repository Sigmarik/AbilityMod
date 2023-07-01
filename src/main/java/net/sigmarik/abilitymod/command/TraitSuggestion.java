package net.sigmarik.abilitymod.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.sigmarik.abilitymod.util.Traits;

import java.util.concurrent.CompletableFuture;

public class TraitSuggestion implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String prefix = builder.getRemainingLowerCase();

        for (String trait : Traits.TRAITS) {
            if (trait.startsWith(prefix)) builder.suggest(trait);
        }

        if (context.getInput().startsWith("/trait remove") && "all".startsWith(prefix)) builder.suggest("all");

        return builder.buildFuture();
    }
}
