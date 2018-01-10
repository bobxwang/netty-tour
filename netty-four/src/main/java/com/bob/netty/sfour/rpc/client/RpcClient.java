package com.bob.netty.sfour.rpc.client;

import com.bob.netty.sfour.rpc.core.RpcDecoder;
import com.bob.netty.sfour.rpc.core.RpcEncoder;
import com.bob.netty.utils.param.RpcResponse;
import com.bob.netty.utils.param.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by bob on 16/7/15.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String host;
    private int port;
    private RpcResponse response;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {

//        try {
//            lock.lock();
        this.response = msg;
//            condition.signalAll();
//        } finally {
//            lock.unlock();
//        }

        countDownLatch.countDown();

        System.out.println(Thread.currentThread().getName() + "channelRead0");
    }

    /**
     * @param request
     * @param timeMilliseconds 超时等待时间,单位秒
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request, int timeMilliseconds) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(RpcClient.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();

//            try {
//
//                lock.lock();

            channel.writeAndFlush(request).sync();
            System.out.println(Thread.currentThread().getName() + "send");

            // 设定下超时时间,10秒
//                condition.await(10 * 1000, TimeUnit.MILLISECONDS);

            countDownLatch.await(timeMilliseconds * 1000, TimeUnit.MILLISECONDS);

            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;

//            } finally {
//                lock.unlock();
//            }

        } finally {
            group.shutdownGracefully();
        }
    }
}