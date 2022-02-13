package com.cubicfox.attendance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.WritableByteChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CountableWritableByteChannel implements WritableByteChannel {

    public static CountableWritableByteChannel of(WritableByteChannel channel) {
        return channel instanceof CountableWritableByteChannel //
                ? (CountableWritableByteChannel) channel //
                : new CountableWritableByteChannel(channel);
    }

    @Getter
    private long count = 0;
    @Delegate(types = Channel.class)
    @NonNull
    private final WritableByteChannel channel;

    @Override
    public int write(ByteBuffer src) throws IOException {
        int writen = channel.write(src);
        this.count += writen;
        return writen;
    }
}
