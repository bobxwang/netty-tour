package com.bob.netty.sfive.rpc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bob on 16/7/20.
 */
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbortPolicyWithReport.class);

    private final String threadName;

    public AbortPolicyWithReport(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Always throws RejectedExecutionException.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     * @throws RejectedExecutionException always.
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("RpcServer["
                        + " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d),"
                        + " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)]",
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
        LOGGER.info(msg);
        throw new RejectedExecutionException(msg);
    }
}