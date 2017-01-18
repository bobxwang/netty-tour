package com.bob.netty.sfive.agreement;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 *
 */
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }
}