package com.example.examplemod;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

// TODO: Rename
public class NewMessage {
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private String _usedItemDescriptionId;

    // TODO: If this is not needed - add a comment to not remove it
    public NewMessage() {}

    public NewMessage(String usedItemDescriptionId) {
        this._usedItemDescriptionId = usedItemDescriptionId;
    }

    public String getUsedItemDescriptionId() {
        return this._usedItemDescriptionId;
    }

    void encode(ByteBuf buffer) {
        var toEncode = this._usedItemDescriptionId;
        buffer.writeCharSequence(toEncode, DEFAULT_CHARSET);
    }

    static NewMessage decode(ByteBuf buffer) {
        var decoded = buffer.readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET);
        var usedItemDescriptionId = decoded.toString();
        return new NewMessage(usedItemDescriptionId);
    }
}
