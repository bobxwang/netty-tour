package com.bob.netty.sfive.http

import java.io.{File, RandomAccessFile}
import java.net.URLDecoder

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.stream.ChunkedWriteHandler

/**
  *
  */
object HttpFileServer {

  final val DEFAULT_URL = "/src/com/bob/netty"

  def run(port: Int, url: String = DEFAULT_URL) = {

    val bossGroup = new NioEventLoopGroup()
    val workerGroup = new NioEventLoopGroup()

    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            // 首先添加HTTP请求消息解码器
            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder)
            // 作用是将多个消息转换为单一的FullHttpRequest或FullHttpResponse,原因是HTTP解码器在每个HTTP消息中会生成多个消息对象
            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536))
            // 增加HTTP响应编码器,对HTTP响应消息进行编码
            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder)
            // 增加Chunked Handler,作用是支持异步发送大的码流(例如文件传输),使不占用过多内存,防止内存溢出
            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler)
            ch.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(url))
          }
        })
      val f = b.bind("127.0.0.1", port).sync()
      f.channel().closeFuture().sync()
    } finally {
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }
  }

  def main(args: Array[String]) {
    run(8080)
  }

}

class HttpFileServerHandler(url: String) extends SimpleChannelInboundHandler[FullHttpRequest] {

  override def messageReceived(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    if (msg.decoderResult().isSuccess && msg.method() == HttpMethod.GET) {
      val uri = msg.uri()
      val path = URLDecoder.decode(uri, "UTF-8")
      val file = new File(path)
      if (file.isHidden || !file.exists() || !file.isFile) {

      } else {
        val randomAccessFile = new RandomAccessFile(file, "r")
        val fileLength = randomAccessFile.length()
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)

      }

    }
  }
}
