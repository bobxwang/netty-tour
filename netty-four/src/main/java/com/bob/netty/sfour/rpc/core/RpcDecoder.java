package com.bob.netty.sfour.rpc.core;

import com.bob.netty.utils.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by bob on 16/7/15.
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clasz;

//    private ObjectMapper objectMapper = new ObjectMapper();

    public RpcDecoder(Class<?> clasz) {
        this.clasz = clasz;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = ProtostuffUtil.deserialize(data, clasz);

//        try {
//            System.out.println("receive -- " + objectMapper.writeValueAsString(obj));
//        } catch (Exception e) {
//            System.out.println("fastxml to serialize has error " + e.getMessage());
//        }

        out.add(obj);
    }
}