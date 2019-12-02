#### Future-Listener 

#### Reactor
> 多路复用(Acceptor)/事件分发(Dispatcher)/事件处理(Handler)
* 单线程模型，所有的I/O操作都在同一个NIO线程上面完成，使用异步非阻塞I/O
* 多线程模型，有专门一个Acctptor线程用于监听服务端，网络IO的读写有一组NIO线程来处理，一个NIO线程可同时处理N条链路，但一个链路只对应一个NIO线程，防止并发操作 
* 主从多线程模型，采用多个Reactor，每个Reactor在自己单独的线程里执行 

#### 主要类
* NioEventLoopGroup/NioEventLoop
* ServerBootstrap
* ChannelPipeline

#### 无锁化的串行设计

通过串行化设计，即消息的处理尽可能在同一个线程内完成，期间避免了线程切换。

#### TCP粘包/拆包解决之道

- LineBasedFrameDecoder 

  ```java
  ch.pipeline().addLast(new LineBasedFrameDecoder(1024)); // 文本解码器
  ```

- FixedLengthFrameDecoder

  ```java
  ch.pipeline().addLast(new FixedLengthFrameDecoder(30));  // 定长解码器
  ```

- DelimiterBasedFrameDecoder 

  ``` java
  // 特殊分隔符解码器
  ```

- LengthFieldBasedFrameDecoder 

  ``` java
  // 基于包头不固定长度的解码器
  ```

#### 心跳机制

``` java
ch.pipeline().addLast(new IdleStateHandler(60,15,30,TimeUnit.SECONDS));
```

#### 可定制性

* 责任链模式 ChannelPipline便于业务拦截，定制扩展
* 基于接口开发
* 提供了大量工厂灰
* 提供大量系统参数用用户按需设置

#### 可扩展性

* Http协议
* Thrift协议
* FTP

#### 零拷贝

* 接收发送ByteBuffer均采用Direct Buffers，使用堆外直接内存进行Socket读写，不需字节缓冲区的二次拷贝
* 提供可组合的Buffer对象，可聚合多个ByteBuffer对象
* 文件传输类 DefaultFileRegion 采用transferTo方法，直接将文件缓冲区数据发送到目标Channel

#### Utils Project
* ColorUtil 打印带颜色的字符串
* ProtostuffUtil Protostuff序列化反序列化工具类