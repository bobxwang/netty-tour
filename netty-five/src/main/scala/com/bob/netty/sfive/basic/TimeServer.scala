package com.bob.netty.sfive.basic

import java.util.concurrent.{ExecutorService, Executors}

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.ReferenceCountUtil

import scala.collection.mutable

object TimeServer {

  def bind(port: Int) = {
    // 创建两个线程组,一个用户接受客户端请求,一个用于进行SocketChannel的网络读写,用于隔离NIO Acceptor和NIO I/O线程
    val bossGroup = new NioEventLoopGroup()
    val workerGroup = new NioEventLoopGroup()

    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .option(ChannelOption.SO_BACKLOG, new Integer(1024))
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            //          ch.pipeline().addLast(new LineBasedFrameDecoder(1024))
            //          ch.pipeline().addLast(new StringDecoder())
            ch.pipeline().addLast(new TimeServerHandler)
          }
        })
      // 绑定端口,同步等待成功
      val f: ChannelFuture = b.bind(port).sync()
      // 阻塞等待服务端监听端口关闭
      f.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  def main(args: Array[String]) {
    bind(8080)
  }
}

/**
  * 用于对网络事件进行读写操作
  */
class TimeServerHandler extends ChannelHandlerAdapter {

  val hashclient = new mutable.HashMap[String, ChannelHandlerContext]()

  val threadPool: ExecutorService = Executors.newFixedThreadPool(5)

  threadPool.execute(new Runnable {
    override def run(): Unit = {
      while (true) {
        Thread.sleep(1000)
        println(hashclient.size)
        hashclient.foreach(x => {
          println(x._1)
          val metadata: ChannelMetadata = x._2.channel().metadata()
          println(metadata)
          //          x._2.writeAndFlush(Unpooled.copiedBuffer(s"now is haha ${x._1}".getBytes))
        })
      }
    }
  })

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    msg match {
      case buf: ByteBuf => try {
        val req = new Array[Byte](buf.readableBytes())
        buf.readBytes(req)
        val body = new String(req, "UTF-8")
        println(s"the time server receiver order: ${body}")
        val ct = if (body == "QUERY TIME ORDER") System.currentTimeMillis().toString else s"BAD ORDER ${body}"
        val resp = Unpooled.copiedBuffer(ct.getBytes)
        // write只是将待发送的消息放到发送缓冲数组中，同时将其发送到下一个ChannelHandler
        ctx.write(resp)
      } finally {
        ReferenceCountUtil.release(msg)
      }
      case _ =>
    }
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    println(ctx.name())
    if (!hashclient.contains(ctx.name())) {
      hashclient.put(ctx.name(), ctx)
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    // flush后将发送缓冲区中的信息全部写到SocketChannel中
    ctx.flush()
  }

  /**
    * 发生异常时,关闭ChannelHandlerContext,释放和ChannelHandlerContext相关联的句柄等资源
    *
    * @param ctx
    * @param cause
    */
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = ctx.close()
}