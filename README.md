#### Reactor

* 单线程模型，所有的I/O操作都在同一个NIO线程上面完成，使用异步非阻塞I/O
* 多线程模型，跟单线程区别就是有一组NIO线程来处理I/O操作，一个NIO线程可同时处理N条链路，但一个链路只对应一个NIO线程
* 主从Reactor 多线程模型

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
* 文件传输采用transferTo方法，直接将文件缓冲区数据发送到目标Channel