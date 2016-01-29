package com.odobo.vertx3.serviceproxy.meta;

import com.odobo.vertx3.serviceproxy.serializer.ProxyJsonSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: plenderyou
 * Date: 13/11/2015
 * Time: 09:43
 *
 * Use this on a parameter to override the serialzation
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyObject {

    Class<? extends ProxyJsonSerializer> serializer();
}
