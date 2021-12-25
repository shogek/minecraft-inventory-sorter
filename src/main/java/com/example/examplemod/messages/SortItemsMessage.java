package com.example.examplemod.messages;

import com.example.examplemod.SortTargets;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class SortItemsMessage implements IMessage {
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final String SEPARATOR = "`";

    private final boolean _isLoggingEnabled;
    private final SortTargets _sortTarget;
    private final boolean _shouldSortByCategory;

    public SortItemsMessage(boolean isLoggingEnabled, SortTargets sortTarget, boolean shouldSortByCategory) {
        _isLoggingEnabled = isLoggingEnabled;
        _sortTarget = sortTarget;
        _shouldSortByCategory = shouldSortByCategory;
    }

    public boolean getIsLoggingEnabled() { return this._isLoggingEnabled; }
    public SortTargets getSortTarget() { return this._sortTarget; }
    public boolean shouldSortByCategory() { return this._shouldSortByCategory; }

    public void encode(ByteBuf buffer) {
        var arguments = new String[] {
            this._isLoggingEnabled + "",
            this._sortTarget.name(),
            this._shouldSortByCategory + "",
        };
        var toEncode = String.join(SEPARATOR, arguments);
        buffer.writeCharSequence(toEncode, DEFAULT_CHARSET);
    }

    public static SortItemsMessage decode(ByteBuf buffer) {
        var arguments = buffer
            .readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET)
            .toString()
            .split(SEPARATOR);

        var isLoggingEnabled = Boolean.parseBoolean(arguments[0]);
        var sortTarget = SortTargets.valueOf(arguments[1]);
        var shouldSortByCategory = Boolean.parseBoolean(arguments[2]);

        return new SortItemsMessage(isLoggingEnabled, sortTarget, shouldSortByCategory);
    }
}
