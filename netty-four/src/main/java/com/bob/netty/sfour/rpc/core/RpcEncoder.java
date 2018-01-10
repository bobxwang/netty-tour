package com.bob.netty.sfour.rpc.core;

import com.bob.netty.utils.ProtostuffUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by bob on 16/7/15.
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> clasz;

    private ObjectMapper objectMapper = new ObjectMapper();

    public RpcEncoder(Class<?> clasz) {
        this.clasz = clasz;
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        boolean a = super.acceptOutboundMessage(msg);

//        System.out.println("acceptOutboundMessage result is " + a + " and input is " + msg);

        return a;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        try {
            System.out.println("encoder " + msg + "--" + this.clasz.getName());
            try {
                System.out.println(msg + " -- " + objectMapper.writeValueAsString(msg));
            } catch (Exception e) {
                System.out.println("encoder fastxml error " + e.getMessage());
            }
        } catch (Exception ee) {
            System.out.println(ee.getMessage());
        }

        if (clasz.isInstance(msg)) {
            byte[] data = ProtostuffUtil.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        } else {
            System.out.println("不是期待的class");
        }
    }
}