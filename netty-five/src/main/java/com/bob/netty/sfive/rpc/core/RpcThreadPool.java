package com.bob.netty.sfive.rpc.core;

import java.util.concurrent.*;

/**
 *
 */
public class RpcThreadPool {

    public static Executor executor(int threads, int queues) {

        BlockingQueue blockingQueue = queues == 0 ? new SynchronousQueue<>() : (queues < 0) ? new LinkedBlockingQueue<>() : new LinkedBlockingQueue<>(queues);

        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                blockingQueue,
                new NamedThreadFactory("RpcThreadPool", true), new AbortPolicyWithReport("RpcThreadPool"));
    }
}