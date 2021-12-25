package com.example.examplemod.handlers;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

/** I log messages to a player's chat. */
public class ChatLogger {
    // TODO: Move to an enum
    private static final String COLOR_GRAY_DARK = "\u00A78";
    private static final String COLOR_GRAY_LIGHT = "\u00A77";
    private static final String COLOR_GREEN = "\u00A7a";
    private static final String COLOR_RED = "\u00A7c";

    private final boolean _isEnabled;
    private final String _namespace;
    private final ServerPlayer _player;

    /**
     * @param caller The calling class (class's name will precede every logged message).
     * @param isEnabled If `false`, all requests to log will be ignored.
     * @param player The player that will receive the log message.
     */
    public ChatLogger(Class caller, boolean isEnabled, ServerPlayer player) {
        _isEnabled = isEnabled;
        _namespace = "[" + caller.getSimpleName() + "]";
        _player = player;
    }

    public void log(String message) {
        log(_isEnabled, _player, _namespace, message, COLOR_GRAY_LIGHT, false);
    }

    public void log(String message, boolean shouldAddSeparator) {
        log(_isEnabled, _player, _namespace, message, COLOR_GRAY_LIGHT, shouldAddSeparator);
    }

    public void logSuccess(String message) {
        log(_isEnabled, _player, _namespace, message, COLOR_GREEN, false);
    }

    public void logSuccess(String message, boolean shouldAddSeparator) {
        log(_isEnabled, _player, _namespace, message, COLOR_GREEN, shouldAddSeparator);
    }

    public void logError(String message) {
        log(_isEnabled, _player, _namespace, message, COLOR_RED, false);
    }

    public void logError(String message, boolean shouldAddSeparator) {
        log(_isEnabled, _player, _namespace, message, COLOR_RED, shouldAddSeparator);
    }

    private static void log(
        boolean isEnabled,
        ServerPlayer player,
        String namespace,
        String message,
        String messageColor,
        boolean shouldAddSeparator
    ) {
        if (!isEnabled || player == null) {
            return;
        }

        var text = "" +
            COLOR_GRAY_DARK + namespace + "\n" +
            messageColor + message +
            (shouldAddSeparator ? COLOR_GRAY_DARK + "\n------" : "");

        var component = new TextComponent(text);
        player.sendMessage(component, Util.NIL_UUID);
    }
}
