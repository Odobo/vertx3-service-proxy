package com.odobo.vertx3.serviceproxy.meta;

import com.odobo.vertx3.serviceproxy.serializer.DefaultJsonSerializer;
import com.odobo.vertx3.serviceproxy.serializer.ProxyJsonSerializer;
import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: plenderyou
 * Date: 11/12/2015
 * Time: 4:50 PM
 *
 * Holds meta-data about methods
 *
 */
public class MethodMeta {
    private final String identifier;
    private final List<ServiceMetaData.Argument> arguments;
    private final ServiceMetaData.Argument returnType;
    private final Method method;

    public MethodMeta(final Method method) {
        identifier = identifierFromMethod(method);
        arguments = parseArguments(method);
        returnType = parseReturnType(method);
        this.method = method;

    }


    public String identifier() {
        return identifier;
    }

    public static String identifierFromMethod(final Method m) {
        final MethodIdentifier mi = m.getDeclaredAnnotation(MethodIdentifier.class);
        return mi == null ? m.getName() : mi.value();
    }

    public ServiceMetaData.Argument getReturnType() {
        return returnType;
    }

    public List<ServiceMetaData.Argument> getArguments() {
        return arguments;
    }

    public Method getMethod() {
        return method;
    }

    private ServiceMetaData.Argument parseReturnType(final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (!parameterTypes[parameterTypes.length - 1].isAssignableFrom(ServiceHandler.class)) {
            throw new RuntimeException("Last paremeter should be of type " + ServiceHandler.class.getSimpleName() + " but is " + parameterTypes[parameterTypes.length - 1].getName());
        }
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        final ParameterizedType t = (ParameterizedType) genericParameterTypes[genericParameterTypes.length - 1];
        final Class<?> returnType;
        try {
            returnType = Class.forName(t.getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final int last = parameterAnnotations.length - 1;
        final ProxyJsonSerializer proxyJsonSerializer = getProxyJsonSerializer(parameterAnnotations[last], returnType);

        return new ServiceMetaData.Argument(proxyJsonSerializer, returnType);
    }

    private List<ServiceMetaData.Argument> parseArguments(final Method method) {
        final List<ServiceMetaData.Argument> arguments = new ArrayList<>();


        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterAnnotations.length - 1; i++) {
            final ProxyJsonSerializer proxyJsonSerializer = getProxyJsonSerializer(parameterAnnotations[i], parameterTypes[i]);
            arguments.add(new ServiceMetaData.Argument(proxyJsonSerializer, parameterTypes[i]));

        }
        return arguments;

    }


    private static ProxyJsonSerializer getProxyJsonSerializer(final Annotation[] annotations, final Class<?> parameterType) {

        return Arrays.asList(annotations).stream().filter(a -> { return a instanceof ProxyObject; })
                     .findFirst()
                     .map(an -> { return (ProxyObject) an; })
                     .map(po -> {
                         try {
                             return po.serializer().newInstance();
                         } catch (Exception e) {
                             return new DefaultJsonSerializer();
                         }
                     })
                     .orElseGet(() -> {
                         return parameterType.isPrimitive() || EventBusTypes.contains(parameterType) ? null : new DefaultJsonSerializer();
                     });

    }

    @Override
    public String toString() {
        return "MethodMeta{" +
            "identifier='" + identifier + '\'' +
            ", arguments=" + arguments +
            ", returnType=" + returnType +
            ", method=" + method +
            '}';
    }
}
