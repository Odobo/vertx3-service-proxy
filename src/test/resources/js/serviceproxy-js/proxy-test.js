var ServiceProxy = require('js/serviceproxy-js/proxy');

var io = Packages.io;

var logger = io.vertx.core.logging.LoggerFactory.getLogger("com.odobo.proxy_test");


exports.vertxStartAsync = function (future) {
    var TestSuite = require('vertx-unit-js/test_suite');
    var suite = TestSuite.create("proxy_test")
        .test("create_proxy_no_handler", function (testContext) {
            // Inherits the vertx
            var proxy = ServiceProxy.createClient(vertx, "some-test-address", com.odobo.vertx3.serviceproxy.SampleInterface.class);

            var async = testContext.async();
            testContext.assertNotNull(proxy);
            var sampleObj = {
                string: "one",
                intValue: 3,
                jsonObject: {
                    fred: 'quimby'
                }
            };
            proxy.method1('string', 3, sampleObj, function (res, err) {
                testContext.assertNotNull(err);
                testContext.assertEquals(-1, err.failureCode);
                testContext.assertNotNull(err.context);
                testContext.assertNull(res);
                async.complete();
            });
        })
        .test("proxy_with_handler", function (testContext) {

            var async = testContext.async();
            var service = ServiceProxy.registerService(vertx, "some-test-address", com.odobo.vertx3.serviceproxy.SampleInterface.class, {
                method1: function (str, int, obj, sh) {
                    sh.ok(obj);
                },
                specialMethod1: function (str, obj, sh) {
                    sh.ok(obj);
                }
            }, function (ex, msg) {
                msg.fail(-1, ex);
            });


            var obj1 = {
                string: "two",
                intValue: 3,
                jsonObject: {
                    fred: 'quimby'
                }
            };
            var proxy = ServiceProxy.createClient(vertx, "some-test-address", com.odobo.vertx3.serviceproxy.SampleInterface.class);

            proxy.method1('string', 2, obj1, function (res, err) {
                testContext.assertNotNull(res);
                logger.debug("Response is " + JSON.stringify(res));
                testContext.assertEquals(obj1.string, res.string);
                testContext.assertNull(err);
                proxy.specialMethod1('str', obj1, function (res, err) {
                    testContext.assertNotNull(res);
                    logger.debug("Response is " + JSON.stringify(res));

                    testContext.assertEquals(obj1.string, res.string);
                    testContext.assertNotNull(service);
                    testContext.assertNull(err);
                    logger.debug("Service function " + typeof service['unregister']);
                    service.unregister(function (res, err) {
                        testContext.assertNull(err);
                        async.complete();
                    });
                });
            });
        })
        .test("proxy_with_handler_Failure", function (testContext) {
            var async = testContext.async();
            var service = ServiceProxy.registerService(vertx, "some-test-address", com.odobo.vertx3.serviceproxy.SampleInterface.class, {
                method1: function (str, int, obj, sh) {
                    sh.fail(99, obj);
                },
                specialMethod1: function (str, obj, sh) {
                    sh.fail(100, obj);
                }
            }, function (ex, msg) {
                msg.fail(-1, ex);
            });

            var obj1 = {
                string: "three",
                intValue: 3,
                jsonObject: {
                    fred: 'quimby'
                }
            };
            var proxy = ServiceProxy.createClient(vertx, "some-test-address", com.odobo.vertx3.serviceproxy.SampleInterface.class);

            proxy.method1('string', 2, obj1, function (res, err) {
                testContext.assertNotNull(err);
                testContext.assertEquals(99, err.failureCode);
                logger.debug("Error is " + JSON.stringify(err));
                testContext.assertEquals(obj1.string, err.context.string);
                testContext.assertNull(res);
                service.unregister(function (res, err) {
                    testContext.assertNull(err);
                    async.complete();
                });

            });
        })
        .test("proxy-call-java-service", function (testContext) {
            var async = testContext.async();
            var obj1 = {
                string: "two",
                intValue: 3,
                jsonObject: {
                    fred: 'quimby'
                }
            };
            var proxy = new ServiceProxy.createClient(vertx, "java-good-service", com.odobo.vertx3.serviceproxy.SampleInterface.class);

            proxy.method1('string', 2, obj1, function (res, err) {
                testContext.assertNotNull(res);
                logger.debug("Response is " + JSON.stringify(res));
                testContext.assertEquals(obj1.string, res.string);
                testContext.assertNull(err);
                proxy.specialMethod1('str', obj1, function (res, err) {
                    testContext.assertNotNull(res);
                    logger.debug("Response is " + JSON.stringify(res));

                    testContext.assertEquals(obj1.string, res.string);
                    testContext.assertNull(err);
                    async.complete();
                });
            });
        });


    suite.run({
        "reporters": [
            {
                "to": "log:junit"
            }
        ]
    }).resolve(future);
};

