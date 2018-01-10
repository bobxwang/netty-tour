package com.bob.netty.sfour.demo

import com.bob.netty.sfour.demo.service.CalculatorService
import com.bob.netty.sfour.rpc.client.CGlibRpcProxy
import com.bob.netty.sfour.rpc.client.async.ConnectManage

/**
  * Created by bob on 16/7/16.
  *
  */
object OneRpcClient {

  def main(args: Array[String]): Unit = {

    ConnectManage.getInstance().start

    val proxy = CGlibRpcProxy.getInstance()
    val h = proxy.getProxy(classOf[CalculatorService])
    (1 to 10).par.foreach(x => {
      try {
        val rs = h.add(x, x)
        println(s"${x} result is ${rs}")
      } catch {
        case e: Exception => println(s"${Console.RED} ${x} has error ${e} ${Console.RESET}")
      }
    })

    println(s"${Console.YELLOW} done ${Console.RESET}")
  }
}