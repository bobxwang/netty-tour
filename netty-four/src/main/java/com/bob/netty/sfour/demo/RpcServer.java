package com.bob.netty.sfour.demo;

import com.bob.netty.sfour.demo.service.CalculatorServiceImpl;
import com.bob.netty.sfour.rpc.core.RpcDecoder;
import com.bob.netty.sfour.rpc.core.RpcEncoder;
import com.bob.netty.sfour.rpc.server.RpcHandler;
import com.bob.netty.utils.param.RpcResponse;
import com.bob.netty.utils.param.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bob on 16/7/16.
 */
public class RpcServer {

    public static void main(String[] args) {

        final Map<String, Object> handlerMap = new HashMap<>();
        // 这里我们如果集成spring,可以扫描所有带RpcService的注解,放到这个handlerMap中
        handlerMap.put("com.bob.netty.sfour.demo.service.CalculatorService", new CalculatorServiceImpl());

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                    // 将请求进行解码
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    // 将响应进行编码
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    // 处理请求
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind("127.0.0.1", 8080).sync();
            System.out.println("service is started and listen on port 8080");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("service want to stop self");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}