package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import com.odobo.vertx3.serviceproxy.meta.MethodIdentifier;
import com.odobo.vertx3.serviceproxy.meta.ProxyObject;
import io.vertx.core.Vertx;

/**
 * User: plenderyou
 * Date: 19/10/15
 * Time: 09:04
 */

public interface SampleInterface {

    static public SampleInterface createProxy(final Vertx vertx, final String address) {
        return ServiceProxy.createClient(vertx, address, SampleInterface.class);
    }

    void method1(final String string, final int intValue, final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler);

    @MethodIdentifier("specialMethod1")
    void method1(final String string, @ProxyObject( serializer = TestJsonSerializer.class) final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler);
}
