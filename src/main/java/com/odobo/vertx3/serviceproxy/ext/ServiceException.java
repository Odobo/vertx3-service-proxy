package com.odobo.vertx3.serviceproxy.ext;

import io.vertx.core.json.JsonObject;

/**
 * User: plenderyou
 * Date: 27/01/2016
 * Time: 11:47 AM
 */
public class ServiceException extends RuntimeException {
    private final JsonObject context;
    private final int errCode;

    public ServiceException(final int errCode, final JsonObject context) {
        super("Service exception errCode="+ errCode);
        this.errCode = errCode;
        this.context = context;
    }

    public JsonObject getContext() {
        return context;
    }

    public int getErrCode() {
        return errCode;
    }
}
