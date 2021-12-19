package com.example.examplemod.commands;

import com.example.examplemod.BoopSorterMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class DebugDisableCommand {
    public DebugDisableCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(DebugDisableCommand.getCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return
            Commands
            .literal("boops")
            .then(
                Commands
                .literal("debug")
                .then(
                    Commands
                    .literal("disable")
                    .executes(DebugDisableCommand::getCommandHandler)
                )
            );
    }

    private static int getCommandHandler(CommandContext<CommandSourceStack> commandContext) {
        BoopSorterMod.disableLogging();

        var emoji = "\u0028\uffe3\u006f\uffe3\u0029"; // (￣o￣)
        var message = new TextComponent(emoji + " " + "DEBUG MODE DISABLED");
        commandContext.getSource().sendSuccess(message, true);

        return 1;
    }
}
