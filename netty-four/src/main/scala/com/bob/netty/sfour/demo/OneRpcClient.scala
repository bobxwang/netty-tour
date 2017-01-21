package com.bob.netty.sfour.demo

import com.bob.netty.sfour.demo.service.CalculatorService
import com.bob.netty.sfour.rpc.client.CGlibRpcProxy

/**
  * Created by bob on 16/7/16.
  *
  */
object OneRpcClient {

  def main(args: Array[String]): Unit = {
    val proxy = CGlibRpcProxy.getInstance()
    val h = proxy.getProxy(classOf[CalculatorService])
    (1 to 1000).par.foreach(x => {
      try {
        val rs = h.add(x, x)
        println(s"${x} result is ${rs}")
      } catch {
        case e: Exception => println(s"${Console.RED} ${x} has error ${e.getMessage} ${Console.RESET}")
      }
    })

    println(s"${Console.YELLOW} done ${Console.RESET}")
  }
}