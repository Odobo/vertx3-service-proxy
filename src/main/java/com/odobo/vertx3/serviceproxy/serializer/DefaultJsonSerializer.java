package com.odobo.vertx3.serviceproxy.serializer;

import com.odobo.vertx3.serviceproxy.serializer.ProxyJsonSerializer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * User: plenderyou
 * Date: 13/11/2015
 * Time: 09:56
 *
 * A default serializer to convert object to Json using the Json class
 *
 * This is used wherever the argument is recognised as a non-eventbus type and there
 * isn't a @ProxyObject specified
 */
public class DefaultJsonSerializer implements ProxyJsonSerializer {

    @Override
    public <C> C fromJson(JsonObject from, Class<C> clazz) {
        return Json.mapper.convertValue(from.getMap(), clazz);
    }

    @Override
    public JsonObject toJson(final Object o) {
        return new JsonObject(Json.mapper.convertValue(o, Map.class));
    }
}
