package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.handler.FailHandler;
import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import com.odobo.vertx3.serviceproxy.meta.ProxyObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * User: plenderyou
 * Date: 14/12/2015
 * Time: 9:49 AM
 */
@RunWith(VertxUnitRunner.class)
public class JavascriptTest {
    private Vertx vertx;

    @Before
    public void before(final TestContext context){
        vertx = Vertx.vertx();

        ServiceProxy.registerService(vertx, "java-good-service", SampleInterface.class, new PingPongService(), (ex, msg)->{
            msg.fail(-1, new JsonObject().put("message", ex.getMessage()));
        });
    }

    @After
    public void after(final TestContext context){
        final Async async = context.async();
        vertx.close(v->{
            async.complete();
        });
    }



    @Test
    public void testProxyStuff(final TestContext context) {
        final Async async = context.async();


        vertx.deployVerticle("js/serviceproxy-js/proxy-test.js", new DeploymentOptions(), stringAsyncResult -> {
            if (stringAsyncResult.succeeded()) {

                async.complete();
            } else {
                context.fail(stringAsyncResult.cause());
            }
        });


    }

    @Test
    public void testJavascriptService(final TestContext testContext){
        final Async async = testContext.async();
        vertx.deployVerticle("javascript-service.js", new DeploymentOptions(), stringAsyncResult -> {
            if (stringAsyncResult.succeeded()) {
                final SampleInterface proxy = ServiceProxy.createClient(vertx, "javascript-service", SampleInterface.class);
                final SamplePojo samplePojo = new SamplePojo().setIntValue(123).setString("value").setJsonObject(new JsonObject().put("v", 1));

                proxy.method1("string", 2, samplePojo, new ServiceHandler<SamplePojo>() {
                    @Override
                    public void ok(final SamplePojo value) {
                        testContext.assertEquals(123, value.getIntValue());
                        async.complete();
                    }

                    @Override
                    public void fail(final int errCode, final JsonObject context) {
                        testContext.fail( context.encode() );
                    }
                });


            } else {
                testContext.fail(stringAsyncResult.cause());
            }
        });
    }


    @Test
    public void testJavascriptServiceReturnsBadData(final TestContext testContext){
        final Async async = testContext.async();
        vertx.deployVerticle("javascript-service.js", new DeploymentOptions(), stringAsyncResult -> {
            if (stringAsyncResult.succeeded()) {
                final SampleInterface proxy = ServiceProxy.createClient(vertx, "javascript-service", SampleInterface.class);
                final SamplePojo samplePojo = new SamplePojo().setIntValue(123).setString("value").setJsonObject(new JsonObject().put("v", 1));

                proxy.method1("string", samplePojo, new ServiceHandler<SamplePojo>() {
                    @Override
                    public void ok(final SamplePojo value) {
                        testContext.fail("No No, this should not happen");
                    }

                    @Override
                    public void fail(final int errCode, final JsonObject context) {
                        testContext.assertEquals( FailHandler.BAD_DESERIALIZATION, errCode);

                        async.complete();
                    }
                });


            } else {
                testContext.fail(stringAsyncResult.cause());
            }
        });
    }

    private class PingPongService implements SampleInterface{

        @Override
        public void method1(final String string, final int intValue, final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler) {
            handler.ok(samplePojo);
        }

        @Override
        public void method1(final String string, @ProxyObject(serializer = TestJsonSerializer.class) final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler) {
            handler.ok(samplePojo);
        }
    }
}
