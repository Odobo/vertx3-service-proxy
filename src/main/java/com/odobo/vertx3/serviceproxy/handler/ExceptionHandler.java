package com.odobo.vertx3.serviceproxy.handler;

/**
 * User: plenderyou
 * Date: 11/12/2015
 * Time: 3:57 PM
 *
 * An interface for exception handling to convert to responses on the message bus
 *
 */
@FunctionalInterface
public interface ExceptionHandler {

    void handle(final Throwable t, final FailHandler failHandler);
}
