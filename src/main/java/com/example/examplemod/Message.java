package com.example.examplemod;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class Message {
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private SortTargets _sortTarget;

    // TODO: If this is not needed - add a comment to not remove it
    public Message() {}

    public Message(SortTargets sortTarget) {
        _sortTarget = sortTarget;
    }

    public SortTargets getSortTarget() {
        return this._sortTarget;
    }

    void encode(ByteBuf buffer) {
        var sortTargetAsString = this._sortTarget.name();
        buffer.writeCharSequence(sortTargetAsString, DEFAULT_CHARSET);
    }

    static Message decode(ByteBuf buffer) {
        var charSequence = buffer.readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET);
        var sortTargetAsString = charSequence.toString();
        var sortTarget = SortTargets.valueOf(sortTargetAsString);
        return new Message(sortTarget);
    }
}
