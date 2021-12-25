package com.example.examplemod.messages;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

public class ReplaceDestroyedItemMessage implements IMessage {
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final String SEPARATOR = "`";

    private final boolean _isLoggingEnabled;
    private final String _destroyedItemId;

    public ReplaceDestroyedItemMessage(boolean isLoggingEnabled, String destroyedItemId) {
        this._isLoggingEnabled = isLoggingEnabled;
        this._destroyedItemId = destroyedItemId;
    }

    public boolean getIsLoggingEnabled() { return this._isLoggingEnabled; }
    public String getDestroyedItemId() { return this._destroyedItemId; }

    public void encode(ByteBuf buffer) {
        var arguments = new String[] {
            this._isLoggingEnabled + "",
            this._destroyedItemId,
        };
        var toEncode = String.join(SEPARATOR, arguments);
        buffer.writeCharSequence(toEncode, DEFAULT_CHARSET);
    }

    public static ReplaceDestroyedItemMessage decode(ByteBuf buffer) {
        var arguments = buffer
            .readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET)
            .toString()
            .split(SEPARATOR);

        var isLoggingEnabled = Boolean.parseBoolean(arguments[0]);
        var usedItemDescriptionId = arguments[1];

        return new ReplaceDestroyedItemMessage(isLoggingEnabled, usedItemDescriptionId);
    }
}
