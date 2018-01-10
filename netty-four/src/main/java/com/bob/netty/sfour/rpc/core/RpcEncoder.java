package com.bob.netty.sfour.rpc.core;

import com.bob.netty.utils.ProtostuffUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bob on 16/7/15.
 */
public class RpcEncoder extends MessageToByteEncoder {

    private final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);

    private Class<?> clasz;

    private ObjectMapper objectMapper = new ObjectMapper();

    public RpcEncoder(Class<?> clasz) {
        this.clasz = clasz;
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        boolean a = super.acceptOutboundMessage(msg);

//      logger.info("acceptOutboundMessage result is " + a + " and input is " + msg);

        return a;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        try {
            logger.info("encoder " + msg + "--" + this.clasz.getName());
            try {
                logger.info(msg + " -- " + objectMapper.writeValueAsString(msg));
            } catch (Exception e) {
                logger.info("encoder fastxml error " + e.getMessage());
            }
        } catch (Exception ee) {
            logger.error(ee.getMessage(), ee);
        }

        if (clasz.isInstance(msg)) {
            byte[] data = ProtostuffUtil.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        } else {
            logger.error("不是期待的class");
        }
    }
}