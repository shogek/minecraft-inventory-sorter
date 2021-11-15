package com.example.examplemod;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class Message {
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private String _source;

    public Message() {}

    public Message(String source) {
        _source = source;
    }

    public String getSource() {
        return this._source;
    }

    void encode(ByteBuf buffer) {
        buffer.writeCharSequence(this._source, DEFAULT_CHARSET);
    }

    static Message decode(ByteBuf buffer) {
        var charSequence = buffer.readCharSequence(buffer.readableBytes(), DEFAULT_CHARSET);
        var source = charSequence.toString();
        return new Message(source);
    }
}
