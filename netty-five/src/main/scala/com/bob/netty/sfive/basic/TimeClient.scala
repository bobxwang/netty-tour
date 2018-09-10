package com.bob.netty.sfive.basic

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

/**
  *
  */
object TimeClient {

  def connect(port: Int, host: String): (ChannelFuture, NioEventLoopGroup) = {
    val group = new NioEventLoopGroup()
    val b = new Bootstrap()
    b.group(group).channel(classOf[NioSocketChannel])
      .option(ChannelOption.TCP_NODELAY, new java.lang.Boolean(true))
      // 设置客户端超时机制
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, new Integer(3000))
      .handler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          //        ch.pipeline().addLast(new LineBasedFrameDecoder(1024))
          //        ch.pipeline().addLast(new StringDecoder())
          ch.pipeline().addLast(new TimeClientHandler)
        }
      })
    val f: ChannelFuture = b.connect(host, port).sync()
    (f, group)
  }

  def main(args: Array[String]) {
    val (f, group) = connect(8080, "127.0.0.1")
    try {
      (1 until 100).foreach(x => {
        val req = "QUERY TIME\n".getBytes()
        val firstMessage = Unpooled.buffer(req.length)
        firstMessage.writeBytes(req)
        f.channel().writeAndFlush(firstMessage)
      })
      f.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
    }
  }
}

class TimeClientHandler extends ChannelHandlerAdapter {

  /**
    * 连接建立，此方法会被调用
    *
    * @param ctx
    */
  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    val req = "QUERY TIME ORDER".getBytes()
    val firstMessage = Unpooled.buffer(req.length)
    firstMessage.writeBytes(req)
    ctx.writeAndFlush(firstMessage)
  }

  /**
    * 服务端返回消息时，此方法被调用
    *
    * @param ctx
    * @param msg
    */
  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    msg match {
      case buf: ByteBuf => {
        val req = new Array[Byte](buf.readableBytes())
        buf.readBytes(req)
        val body = new String(req, "UTF-8")
        println(s"Now is ${body}")
      }
      case _ =>
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = ctx.close()
}
