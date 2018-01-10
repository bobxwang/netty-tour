package com.bob.netty.sfour.rpc.core.async;

import com.bob.netty.utils.param.RpcRequest;
import com.bob.netty.utils.param.RpcResponse;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangxiang on 18/1/10.
 */
public class RpcFuture implements Future<Object> {

    private final Logger logger = LoggerFactory.getLogger(RpcFuture.class);
    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;
    private long responseTimeThreshold = 5000;
    private long startTime;
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.response != null) {
            return this.response.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + "; Request class name: " + this.request.getClassName()
                    + "; Request method: " + this.request.getMethodName());
        }
    }

    public void done(RpcResponse rpcResponse) {
        this.response = rpcResponse;
        sync.release(1);
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " + rpcResponse.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private List<RpcCallback> pendingCallbacks = new ArrayList<>();

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final RpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(16);

    private void runCallback(RpcCallback callback) {
        final RpcResponse res = this.response;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!res.isError()) {
                    callback.success(res.getResult());
                } else {
                    callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
                }
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;
        private final int done = 1;
        private final int pending = 0;

        protected boolean tryAcquire(int acquires) {
            return getState() == done ? true : false;
        }

        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            return getState() == done;
        }
    }
}