package com.example.examplemod;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class Message {
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final String SEPARATOR = " ";
    private SortTargets _sortTarget;
    private boolean _shouldSortByCategory;

    // TODO: If this is not needed - add a comment to not remove it
    public Message() {}

    public Message(SortTargets sortTarget, boolean shouldSortByCategory) {
        _sortTarget = sortTarget;
        _shouldSortByCategory = shouldSortByCategory;
    }

    public SortTargets getSortTarget() {
        return this._sortTarget;
    }

    public boolean shouldSortByCategory() {
        return this._shouldSortByCategory;
    }

    void encode(ByteBuf buffer) {
        var toEncode = this._sortTarget.name() + SEPARATOR + this._shouldSortByCategory;
        buffer.writeCharSequence(toEncode, DEFAULT_CHARSET);
    }

    static Message decode(ByteBuf buffer) {
        var charSequence = buffer.readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET);
        var decoded = charSequence.toString().split(SEPARATOR);

        var sortTarget = SortTargets.valueOf(decoded[0]);
        var shouldSortByCategory = Boolean.parseBoolean(decoded[1]);

        return new Message(sortTarget, shouldSortByCategory);
    }
}
