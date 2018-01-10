package com.bob.netty.sfour.rpc.client.async;

import com.bob.netty.sfour.rpc.core.async.RpcFuture;
import com.bob.netty.utils.param.RpcRequest;
import com.bob.netty.utils.param.RpcResponse;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wangxiang on 18/1/10.
 */
public class AsyncRpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncRpcClient.class);
    private volatile Channel channel;
    private ConcurrentHashMap<String, RpcFuture> pendingRpcFuture = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {

        String requestId = rpcResponse.getRequestId();
        RpcFuture rpcFuture = pendingRpcFuture.get(requestId);
        if (rpcFuture != null) {
            pendingRpcFuture.remove(requestId);
            rpcFuture.done(rpcResponse);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client caught exception" + cause.getMessage(), cause);
        ctx.close();
    }

    public RpcFuture send(RpcRequest request) {
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRpcFuture.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("send request " + request.getRequestId() + "has error: " + e.getMessage(), e);
        }

        return rpcFuture;
    }
}