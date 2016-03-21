package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import com.odobo.vertx3.serviceproxy.meta.ProxyObject;

/**
 * User: plenderyou
 * Date: 21/03/2016
 * Time: 3:59 PM
 */

public class PingPongService implements SampleInterface{

    public static final String SOME_STRING = "SomeString";

    @Override
    public void method1(final String string, final int intValue, final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler) {
        handler.ok(samplePojo);
    }

    @Override
    public void method1(final String string, @ProxyObject(serializer = TestJsonSerializer.class) final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler) {
        handler.ok(samplePojo);
    }

    @Override
    public void noArguments(final ServiceHandler<String> handler) {
        handler.ok(SOME_STRING);
    }
}
