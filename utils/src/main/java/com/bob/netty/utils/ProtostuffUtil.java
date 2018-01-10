package com.bob.netty.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.ObjenesisHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bob on 16/7/15.
 */
public class ProtostuffUtil {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private ProtostuffUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clasz) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(clasz);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(clasz);
            if (schema != null) {
                cachedSchema.put(clasz, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化
     *
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> clasz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clasz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     *
     * @param data
     * @param clasz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clasz) {
        try {
            T message = ObjenesisHelper.newInstance(clasz);
            Schema<T> schema = getSchema(clasz);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}