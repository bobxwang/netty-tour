package com.bob.netty.sfour.rpc.core;

import com.bob.netty.utils.ProtostuffUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        String jmsg = "";
        try {
            jmsg = objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException ee) {
            logger.error(ee.getMessage(), ee);
        }
        logger.info("encoder --" + this.clasz.getName() + " -- msg -- " + jmsg);

        if (clasz.isInstance(msg)) {
            byte[] data = ProtostuffUtil.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        } else {
            logger.error("不是期待的class");
        }
    }
}