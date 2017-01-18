package com.bob.netty.sfive.rpc.model;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class MessageReponse implements Serializable {

    private String messageId;

    private String error;

    private Object resultDesc;
}