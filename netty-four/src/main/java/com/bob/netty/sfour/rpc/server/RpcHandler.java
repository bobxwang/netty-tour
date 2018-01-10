package com.bob.netty.sfour.rpc.server;

import com.bob.netty.utils.ColorUtil;
import com.bob.netty.utils.param.RpcRequest;
import com.bob.netty.utils.param.RpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by bob on 16/7/15.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    private ObjectMapper objectMapper = new ObjectMapper();

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println(ColorUtil.BLUE + "active" + ColorUtil.RESET);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println(ColorUtil.BLUE + "inactive" + ColorUtil.RESET);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exception", cause);
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RpcRequest msg) throws Exception {

        String host = ctx.channel().remoteAddress().toString();
        System.out.println(ColorUtil.BLUE + "client address is -> " + host + " handler receive -> " + objectMapper.writeValueAsString(msg) + ColorUtil.RESET);

        RpcResponse response = new RpcResponse();
        response.setRequestId(msg.getRequestId());

        try {
            String className = msg.getClassName();
            Object serviceBean = handlerMap.get(className);
            Class<?> serviceClass = serviceBean.getClass();

            String methodName = msg.getMethodName();
            Class<?>[] parameterTypes = msg.getParameterTypes();
            Object[] parameters = msg.getParameters();

            // 利用cglib进行反射调用
            FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
            Object object = serviceFastMethod.invoke(serviceBean, parameters);
            response.setResult(object);
        } catch (Exception e) {
            response.setError(e);
        } finally {
            final String color = response.isError() ? ColorUtil.RED : ColorUtil.BLUE;
            System.out.println(color + "handler result -> " + objectMapper.writeValueAsString(response) + ColorUtil.RESET);
            ctx.writeAndFlush(response).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    System.out.println(color + "Send response for request -> " + msg.getRequestId() + ColorUtil.RESET);
                    ctx.close();
                }
            });
        }
    }
}