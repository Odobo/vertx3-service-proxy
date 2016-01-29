package com.odobo.vertx3.serviceproxy.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: plenderyou
 * Date: 13/11/2015
 * Time: 09:43
 *
 * Override the name of the method
 *
 * Only really used if there are 2 methods of the same type
 *
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodIdentifier {

    String value();
}
