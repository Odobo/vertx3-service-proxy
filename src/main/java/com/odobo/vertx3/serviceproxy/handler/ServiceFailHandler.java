package com.odobo.vertx3.serviceproxy.handler;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * User: plenderyou
 * Date: 27/01/2016
 * Time: 1:02 PM
 */
public class ServiceFailHandler<T> implements FailHandler {
    final Message<T> message;

    public ServiceFailHandler(final Message<T> message) {
        this.message = message;
    }

    @Override
    public void fail(final int errCode, final JsonObject context) {
        message.fail(errCode, context.encode());

    }
}
