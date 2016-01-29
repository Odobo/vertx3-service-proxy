package com.odobo.vertx3.serviceproxy.serializer;

import io.vertx.core.json.JsonObject;

/**
 * User: plenderyou
 * Date: 13/11/2015
 * Time: 10:02
 *
 * Interface to implement for special serialization
 *
 * So if you wanted to use a long for a date or something
 *
 */
public interface ProxyJsonSerializer {
    <C> C fromJson(JsonObject from, Class<C> clazz);

    JsonObject toJson(Object o);
}
