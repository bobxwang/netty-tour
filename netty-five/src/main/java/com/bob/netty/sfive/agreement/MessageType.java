package com.bob.netty.sfive.agreement;

/**
 *
 */
public enum MessageType {

    LOGIN_RESP("服务认证响应", (byte) 0),
    LOGIN_REQ("客户认证请求", (byte) 1),
    HEARTBEAT_RESP("服务心跳响应", (byte) 2),
    HEARTBEAT_REQ("客户心跳请求", (byte) 3);

    private String mark;
    private byte value;

    MessageType(String mark, byte value) {
        this.mark = mark;
        this.value = value;
    }

    public static String getName(byte value) {
        for (MessageType c : MessageType.values()) {
            if (c.value == value) {
                return c.mark;
            }
        }
        return null;
    }

    public byte value() {
        return this.value;
    }
}