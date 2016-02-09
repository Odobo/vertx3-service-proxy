package com.odobo.vertx3.serviceproxy.meta;

import com.odobo.vertx3.serviceproxy.serializer.ProxyJsonSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * User: plenderyou
 * Date: 08/12/2015
 * Time: 12:59 PM
 *
 * Holds meta-data about a service
 */
public class ServiceMetaData {

    private final Map<String, MethodMeta> methods = new HashMap();

    public ServiceMetaData(final Class serviceInterface) {

        Objects.requireNonNull(serviceInterface);

        if (!serviceInterface.isInterface()) {
            throw new RuntimeException("Proxy " + serviceInterface.getClass().getName() + " should be an interface");
        }


        final Method[] declaredMethods = serviceInterface.getDeclaredMethods();

        for (Method m : declaredMethods) {
            if(Modifier.isStatic(m.getModifiers()) ) continue;
            MethodMeta mm = analyzeMethod(m);
            final MethodMeta oldMethod = methods.put(mm.identifier(), mm);
            if (oldMethod != null) {
                throw new RuntimeException("Duplicate method declaration for " + mm);
            }
        }

    }

    public Map<String, MethodMeta> getMethods() {
        return methods;
    }

    public MethodMeta getMeta(final Method method) {
        return methods.get( MethodMeta.identifierFromMethod(method));
    }

    private MethodMeta analyzeMethod(final Method m) {
        return new MethodMeta(m);
    }

    public MethodMeta getMeta(final String methodName) {
        return methods.get(methodName);

    }

    public static class Argument {
        private final ProxyJsonSerializer serializer;
        private final Class<?> type;

        public Argument(final ProxyJsonSerializer serializer, final Class<?> type) {
            this.serializer = serializer;
            this.type = type;
        }


        public ProxyJsonSerializer getSerializer() {
            return serializer;
        }

        public Class<?> getType() {
            return type;
        }
    }
}
