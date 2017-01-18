package com.bob.netty.sfive.agreement;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 *
 */
public class MsgpackEncoder extends MessageToByteEncoder<NettyMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf out) throws Exception {

        MessagePack msgpack = new MessagePack();
        byte[] raw = msgpack.write(msg);
        out.writeBytes(raw);
    }
}