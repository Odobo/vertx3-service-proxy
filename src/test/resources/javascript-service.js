/**
 * Created by plenderyou on 15/12/2015.
 */
var ServiceProxy = require('js/serviceproxy-js/proxy')
exports.vertxStartAsync = function (future) {

    var service = ServiceProxy.registerService(vertx, "javascript-service", com.odobo.vertx3.serviceproxy.SampleInterface.class, {
        method1: function (str, int, obj, sh) {
            sh.ok(obj);
        },
        specialMethod1: function (str, obj, sh) {
            sh.ok( { some: "crazy-object"});
        },
        noArguments: function(sh) {
            sh.ok("SomeString");
        }
    });


    service.completionHandler( function() {
        future.complete()
    });

};
