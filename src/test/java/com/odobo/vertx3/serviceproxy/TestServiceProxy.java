package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.ext.CompletableFutureHandler;
import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import com.odobo.vertx3.serviceproxy.meta.ProxyObject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * User: plenderyou
 * Date: 19/10/15
 * Time: 09:39
 */
@RunWith(VertxUnitRunner.class)
public class TestServiceProxy {
    private static final Logger logger = LoggerFactory.getLogger(TestServiceProxy.class);

    public static final String SOME_SERVICE_PROXY_ADDRESS = "SOME_SERVICE_PROXY_ADDRESS";
    private Vertx vertx;

    @Before
    public void before(final TestContext context){
        this.vertx = Vertx.vertx();
        logger.debug("before: Registering service {} at {}", SampleInterface.class.getName(), SOME_SERVICE_PROXY_ADDRESS);
        ServiceProxy.registerService(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class, new PingPongService1(), (ex, msg)->{
            msg.fail(999, new JsonObject().put("error", "We got an exception " + ex.getMessage()));
        });

    }

    @Test
    public void testServiceProxy(final TestContext testContext){
        final Async async = testContext.async();


        SampleInterface p = ServiceProxy.createClient(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class);
        final SamplePojo samplePojo = new SamplePojo().setIntValue(123).setString("value").setJsonObject(new JsonObject().put("v", 1));

        p.method1("string", 3, samplePojo, new ServiceHandler<SamplePojo>() {
            @Override
            public void ok(final SamplePojo value) {
                testContext.assertEquals(samplePojo, value);
                async.complete();
            }

            @Override
            public void fail(final int errCode, final JsonObject context) {
                testContext.fail(context.encode());
            }

        });

    }

    @Test
    public void testServiceProxyCompletableFuture(final TestContext testContext){
        final Async async = testContext.async();


        SampleInterface p = ServiceProxy.createClient(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class);
        final SamplePojo samplePojo = new SamplePojo().setIntValue(123).setString("value").setJsonObject(new JsonObject().put("v", 1));

        CompletableFutureHandler<SamplePojo> cf = new CompletableFutureHandler<>();
        p.method1(null, 3, samplePojo, cf.toHandler());

        cf.whenComplete( (value, ex) ->{
            testContext.assertNull(ex);
            testContext.assertEquals(samplePojo, value);
            async.complete();
        });
    }


    @Test
    public void testServiceProxyWithException(final TestContext testContext){
        final Async async = testContext.async();


        SampleInterface p = ServiceProxy.createClient(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class);
        final SamplePojo samplePojo = new SamplePojo().setIntValue(123).setString("value").setJsonObject(new JsonObject().put("v", 1));

        p.method1("string", samplePojo, new ServiceHandler<SamplePojo>() {
            @Override
            public void ok(final SamplePojo value) {
                testContext.fail("Nope, Should not get here");
            }

            @Override
            public void fail(final int errCode, final JsonObject context) {
                testContext.assertEquals(999, errCode);
                testContext.assertEquals("We got an exception Hello", context.getString("error"));
                async.complete();
            }

        });

    }

    @Test
    public void testMethodWithNoParams(final TestContext testContext){
        final Async async = testContext.async();
        SampleInterface p = ServiceProxy.createClient(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class);
        final SamplePojo samplePojo = new SamplePojo().setIntValue(123).setString("value").setJsonObject(new JsonObject().put("v", 1));

        p.noArguments( new ServiceHandler<String>() {
            @Override
            public void ok(final String value) {
                testContext.assertEquals(PingPongService1.SOME_STRING, value);
                async.complete();

            }

            @Override
            public void fail(final int errCode, final JsonObject context) {
                testContext.fail();
            }

        });

    }


    private static class PingPongService1 implements SampleInterface {

        public static final String SOME_STRING = "SomeString";

        @Override
        public void method1(final String string, final int intValue, final SamplePojo samplePojo, ServiceHandler<SamplePojo> handler) {
            handler.ok(samplePojo);

        }

        @Override
        public void method1(final String string, final SamplePojo samplePojo, @ProxyObject( serializer = TestJsonSerializer.class) final ServiceHandler<SamplePojo> handler) {
            throw new RuntimeException("Hello");
        }

        @Override
        public void noArguments(final ServiceHandler<String> handler) {
            handler.ok(SOME_STRING);
        }
    }


}
