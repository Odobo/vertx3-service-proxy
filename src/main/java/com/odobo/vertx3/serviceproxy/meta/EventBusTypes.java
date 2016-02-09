package com.odobo.vertx3.serviceproxy.meta;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * User: plenderyou
 * Date: 11/12/2015
 * Time: 4:54 PM
 * Class that lists the current types not to convert (i.e. raw)
 *
 * Additional Types can be added here
 *
 */
public class EventBusTypes {

    private static final Set<Class<?>> eventBusTypes;

    static {
        eventBusTypes = new HashSet<>();
        eventBusTypes.add(String.class);
        eventBusTypes.add(Buffer.class);
        eventBusTypes.add(JsonObject.class);
        eventBusTypes.add(JsonArray.class);
        eventBusTypes.add(byte[].class);
        eventBusTypes.add(Integer.class);
        eventBusTypes.add(Long.class);
        eventBusTypes.add(Float.class);
        eventBusTypes.add(Double.class);
        eventBusTypes.add(Boolean.class);
        eventBusTypes.add(Short.class);
        eventBusTypes.add(Character.class);
        eventBusTypes.add(Byte.class);

    }
    public static void registerEventBusType(final Class<?> clazz) {
        eventBusTypes.add(clazz);
    }

    public static boolean contains(final Class<?> clazz) {
        return eventBusTypes.contains(clazz);
    }
}
