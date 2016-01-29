package com.odobo.vertx3.serviceproxy.ext;

import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

/**
 * User: plenderyou
 * Date: 27/01/2016
 * Time: 11:44 AM
 */
public class CompletableFutureHandler<T> extends CompletableFuture<T> {

    public ServiceHandler<T> toHandler() {
        return new ServiceHandler<T>() {
            @Override
            public void ok(final T value) {
                complete(value);
            }

            @Override
            public void fail(final int errCode, final JsonObject context) {
                completeExceptionally(new ServiceException(errCode, context));
            }
        };
    }


}
