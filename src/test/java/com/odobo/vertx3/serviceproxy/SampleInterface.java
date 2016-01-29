package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import com.odobo.vertx3.serviceproxy.meta.MethodIdentifier;
import com.odobo.vertx3.serviceproxy.meta.ProxyObject;

/**
 * User: plenderyou
 * Date: 19/10/15
 * Time: 09:04
 */

public interface SampleInterface {

    void method1(final String string, final int intValue, final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler);

    @MethodIdentifier("specialMethod1")
    void method1(final String string, @ProxyObject( serializer = TestJsonSerializer.class) final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler);
}
