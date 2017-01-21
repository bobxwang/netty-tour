package com.bob.netty.sfour.demo.service;

import com.bob.netty.sfour.rpc.server.RpcService;

/**
 * Created by bob on 16/7/16.
 */
@RpcService(clasz = CalculatorService.class)
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public int add(int num1, int num2) {
        return num1 + num2;
    }
}