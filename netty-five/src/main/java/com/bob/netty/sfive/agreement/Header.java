package com.bob.netty.sfive.agreement;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Data
public final class Header {

    private int crcCode = 0xabef0101;

    /**
     * 消息长度
     */
    private int length;

    /**
     * 会话ID
     */
    private long sessionId;

    /**
     * 消息类型
     */
    private byte type;

    /**
     * 消息优先级
     */
    private byte priority;

    /**
     * 附件
     */
    private Map<String, Object> attachment = new HashMap<>();
}