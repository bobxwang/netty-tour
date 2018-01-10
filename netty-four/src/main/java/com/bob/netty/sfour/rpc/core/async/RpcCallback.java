package com.bob.netty.sfour.rpc.core.async;

/**
 * Created by wangxiang on 18/1/10.
 */
public interface RpcCallback {

    void success(Object obj);

    void fail(Exception e);
}