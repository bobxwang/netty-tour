package com.bob.netty.utils;

/**
 * @Author: WangXiang
 * @Date: 2018/9/11 上午9:45
 */
public final class Utils {

    /**
     * 判断所给数是不是2的幂次方
     *
     * @param val
     * @return
     */
    public static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }
}