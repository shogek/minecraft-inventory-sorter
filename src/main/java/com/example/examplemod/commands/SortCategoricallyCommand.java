package com.example.examplemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class SortCategoricallyCommand {
    public SortCategoricallyCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(SortCategoricallyCommand.getCommand());
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
                    .literal("categorically")
                    .executes(SortCategoricallyCommand::getCommandHandler)
                )
            );
    }

    private static int getCommandHandler(CommandContext<CommandSourceStack> commandContext) {
        // TODO: Implement me
        var message = new TextComponent("SORTING CATEGORICALLY");
        commandContext.getSource().sendSuccess(message, true);
        return 1;
    }
}
