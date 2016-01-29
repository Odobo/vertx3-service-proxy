package com.odobo.vertx3.serviceproxy.handler;

/**
 * User: plenderyou
 * Date: 19/10/15
 * Time: 09:08
 *
 * Standard response interface
 *
 */
public interface ServiceHandler<T> extends FailHandler {
    void ok(final T value);
}
