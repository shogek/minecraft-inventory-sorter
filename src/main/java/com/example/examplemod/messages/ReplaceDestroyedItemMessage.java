package com.example.examplemod.messages;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class ReplaceDestroyedItemMessage {
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    private final String _destroyedItemId;

    /** @param destroyedItemId `getDescriptionId()` of an item that needs to be replaced in current hand. */
    public ReplaceDestroyedItemMessage(String destroyedItemId) {
        this._destroyedItemId = destroyedItemId;
    }

    public String getDestroyedItemId() {
        return this._destroyedItemId;
    }

    public void encode(ByteBuf buffer) {
        var toEncode = this._destroyedItemId;
        buffer.writeCharSequence(toEncode, DEFAULT_CHARSET);
    }

    public static ReplaceDestroyedItemMessage decode(ByteBuf buffer) {
        var decoded = buffer.readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET);
        var usedItemDescriptionId = decoded.toString();
        return new ReplaceDestroyedItemMessage(usedItemDescriptionId);
    }
}
