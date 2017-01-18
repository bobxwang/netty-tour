package com.bob.netty.sfive.http

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

/**
  * Created by bob on 16/8/31.
  */
trait AbstractHttpXmlEncoder[T] extends MessageToMessageEncoder[T] {

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = ctx.close()
}