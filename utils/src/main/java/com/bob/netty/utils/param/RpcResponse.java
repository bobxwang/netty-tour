package com.bob.netty.utils.param;

/**
 * Created by bob on 16/7/15.
 */
public class RpcResponse {

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Boolean isError() {
        return this.error != null;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    private String requestId;
    private Throwable error;
    private Object result;

}