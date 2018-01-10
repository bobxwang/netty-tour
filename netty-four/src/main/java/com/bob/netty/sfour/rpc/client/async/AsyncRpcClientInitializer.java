package com.bob.netty.sfour.rpc.client.async;

import com.bob.netty.sfour.rpc.core.RpcDecoder;
import com.bob.netty.sfour.rpc.core.RpcEncoder;
import com.bob.netty.utils.param.RpcRequest;
import com.bob.netty.utils.param.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by wangxiang on 18/1/10.
 */
public class AsyncRpcClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcEncoder(RpcRequest.class));
        cp.addLast(new RpcDecoder(RpcResponse.class));
        cp.addLast(new AsyncRpcClient());
    }
}