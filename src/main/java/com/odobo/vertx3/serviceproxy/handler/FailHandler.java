package com.odobo.vertx3.serviceproxy.handler;

import io.vertx.core.json.JsonObject;

/**
 * User: plenderyou
 * Date: 27/01/2016
 * Time: 12:59 PM
 */
@FunctionalInterface
public interface FailHandler {
    int BAD_DESERIALIZATION = -99;

    void fail(int errCode, JsonObject context);
}
