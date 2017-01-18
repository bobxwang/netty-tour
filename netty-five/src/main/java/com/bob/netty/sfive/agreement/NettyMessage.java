package com.bob.netty.sfive.agreement;

import lombok.Data;

/**
 *
 */
@Data
public class NettyMessage {

    /**
     * 消息头
     */
    private Header header;

    /**
     * 消息体
     */
    private Object body;
}