package com.bob.netty.sfour.rpc.client.async;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接管理
 * Created by wangxiang on 18/1/10.
 */
public class ConnectManage {

    private static final Logger logger = LoggerFactory.getLogger(ConnectManage.class);

    private static class ConnectManageHolder {
        private static ConnectManage instance = new ConnectManage();
    }

    public static ConnectManage getInstance() {
        return ConnectManageHolder.instance;
    }

    private CopyOnWriteArrayList<AsyncRpcClient> connectedHandlers = new CopyOnWriteArrayList<>();

    private ConnectManage() {
    }

    /**
     * 实际工作中可用列表可从某注册中心获取
     */
    public void start() {
        // 模拟多个可用服务列表
        for (int i = 0; i < 5; i++) {
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new AsyncRpcClientInitializer())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true);

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        logger.debug("Successfully connect to remote server. remote peer = 127.0.0.1:8080");
                        AsyncRpcClient handler = channelFuture.channel().pipeline().get(AsyncRpcClient.class);
                        connectedHandlers.add(handler);
                    }
                }
            });
        }
    }

    private AtomicInteger roundRobin = new AtomicInteger(0);

    public AsyncRpcClient chooseAsyncRpcClient() {
        CopyOnWriteArrayList<AsyncRpcClient> handlers = (CopyOnWriteArrayList<AsyncRpcClient>) this.connectedHandlers.clone();
        int size = handlers.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return handlers.get(index);
    }
}