package com.example.examplemod.commands;

import com.example.examplemod.BoopSorterMod;
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
        BoopSorterMod.setSortingByCategory();

        var emoji = "\u1566\u0028\u00f2\u005f\u00f3\u02c7\u0029\u1564"; // ᕦ(ò_óˇ)ᕤ
        var message = new TextComponent(emoji + " " + "SORTING CATEGORICALLY");
        commandContext.getSource().sendSuccess(message, true);

        return 1;
    }
}
