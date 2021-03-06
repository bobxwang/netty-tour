package com.bob.netty.sfour.rpc.client;

import com.bob.netty.sfour.rpc.client.async.AsyncRpcClient;
import com.bob.netty.sfour.rpc.client.async.ConnectManage;
import com.bob.netty.sfour.rpc.core.async.RpcFuture;
import com.bob.netty.utils.param.RpcResponse;
import com.bob.netty.utils.param.RpcRequest;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by bob on 16/7/15.
 */
public class CGlibRpcProxy implements MethodInterceptor {

    private static class CGLibProxyHolder {
        private static CGlibRpcProxy instance = new CGlibRpcProxy();
    }

    private String host = "127.0.0.1";
    private int port = 8080;

    private CGlibRpcProxy() {
    }

    public static CGlibRpcProxy getInstance() {
        return CGLibProxyHolder.instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clasz) {
        return (T) Enhancer.create(clasz, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return o == objects[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(o);
            } else if ("toString".equals(name)) {
                return o.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(o)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(objects);

        AsyncRpcClient asyncRpcClient = ConnectManage.getInstance().chooseAsyncRpcClient();
        RpcFuture rpcFuture = asyncRpcClient.send(request);
        return rpcFuture.get();
    }

    /**
     * 同步调用
     *
     * @param request
     * @return
     * @throws Throwable
     */
    private Object runSync(RpcRequest request) throws Throwable {
        /**
         * 这里每次调用的send时候才去和服务端建立连接，
         * 使用的是短连接，这种短连接在高并发时会有连接数问题，也会影响性能
         */
        RpcClient client = new RpcClient(host, port);
        RpcResponse response = client.send(request, 5);
        if (response == null) {
            throw new RuntimeException("resonse is null");
        }
        if (response.getError() != null) {
            throw response.getError();
        } else {
            return response.getResult();
        }
    }
}