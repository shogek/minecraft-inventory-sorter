package com.example.examplemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class SortAlphabeticallyCommand {
    public SortAlphabeticallyCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(SortAlphabeticallyCommand.getCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return
            Commands
            .literal("boops")
            .then(
                Commands
                .literal("sort")
                .then(
                    Commands
                    .literal("alphabetically")
                    .executes(SortAlphabeticallyCommand::getCommandHandler)
                )
            );
    }

    private static int getCommandHandler(CommandContext<CommandSourceStack> commandContext) {
        // TODO: Implement me
        var message = new TextComponent("SORTING ALPHABETICALLY");
        commandContext.getSource().sendSuccess(message, true);
        return 1;
    }
}
