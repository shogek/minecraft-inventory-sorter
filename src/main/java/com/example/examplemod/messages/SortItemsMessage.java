package com.example.examplemod.messages;

import com.example.examplemod.SortTargets;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class SortItemsMessage implements IMessage {
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final String SEPARATOR = " ";

    private final SortTargets _sortTarget;
    private final boolean _shouldSortByCategory;

    public SortItemsMessage(SortTargets sortTarget, boolean shouldSortByCategory) {
        _sortTarget = sortTarget;
        _shouldSortByCategory = shouldSortByCategory;
    }

    public SortTargets getSortTarget() {
        return this._sortTarget;
    }

    public boolean shouldSortByCategory() {
        return this._shouldSortByCategory;
    }

    public void encode(ByteBuf buffer) {
        var toEncode = this._sortTarget.name() + SEPARATOR + this._shouldSortByCategory;
        buffer.writeCharSequence(toEncode, DEFAULT_CHARSET);
    }

    public static SortItemsMessage decode(ByteBuf buffer) {
        var charSequence = buffer.readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET);
        var decoded = charSequence.toString().split(SEPARATOR);

        var sortTarget = SortTargets.valueOf(decoded[0]);
        var shouldSortByCategory = Boolean.parseBoolean(decoded[1]);

        return new SortItemsMessage(sortTarget, shouldSortByCategory);
    }
}
