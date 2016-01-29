# Service Proxy

This project provides a simple service proxy implementation for vert.x

## raison d'etre

I originally used the https://github.com/vert-x3/vertx-service-proxy but got frustrated with some of the limitations namely:

1. Only being able to use a restricted set of Types for parameters and return types
1. Not being able to use Generic types
1. Not being able to handle failures nicely, i.e. The generated class always returned -1 in the fail(), meaning I had to define my own templates


So taking inspriation from various client proxies from web service libraries, the code utilises java.lang.reflect.Proxy to implement the client and reflection to implement the server part of the service.

## Maven

To utilse this functionality use the maven artifact

~~~~xml
<dependency>
    <groupId>com.odobo.vertx3</groupId>
    <artifactId>service-proxy</artifactId>
    <version>${service.proxy.version}</version>
</dependency>
~~~~

The dependency definitions are all in `provided` scope, so it should work with vert.x 3.1 onwards until the eventbus api changes.

## How to use the functionality


### Definition

The first step is to define a service as an interface in Java for example:

~~~~java
public interface SampleInterface {

    void method1(final String string, final int intValue, final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler);
   
    @MethodIdentifier("specialMethod1")
    void method1(final String string, final SamplePojo samplePojo, final ServiceHandler<SamplePojo> handler);

}
~~~~

There are some restrictions :)

- The final parameter must be a `SerivceHandler<T>`
- methods need to be unique; by default the method name is used however as in the example above a `@MethodIdentifier` annotation can be used.

### Implement the service

The interface needs to be implemented (at the moment java or javascript can be used)

~~~~java
public class PingPongService implements SampleInterface {

        @Override
        public void method1(final String string, final int intValue, final SamplePojo samplePojo, ServiceHandler<SamplePojo> handler) {
            handler.ok(samplePojo);
        }

        @Override
        public void method1(final String string, final SamplePojo samplePojo, @ProxyObject( serializer = TestJsonSerializer.class) final ServiceHandler<SamplePojo> handler) {
            // Some implementation
        }
    }

~~~~

Or

~~~~javascript
var s = {
        method1: function (str, int, obj, sh) {
            sh.ok(obj);
        },
        specialMethod1: function (str, obj, sh) {
            // some implementation
        }
    };
~~~~

In javascript you must use any MethodIdentifiers for the method name

The ServiceHandler has 2 methods:

1. `ok( object )` - where you return the result
2. `fail( errCode, jsonObject )` - where you can return a code and some json context.

### Register the service

Registering the service requires and event bus address so that clients can call it. Use the static method on `registerService` on the `ServiceProxy` class.

~~~~java
ServiceProxy.registerService(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class, new PingPongService(), (ex, msg)->{
            msg.fail(999, new JsonObject().put("error", "We got an exception " + ex.getMessage()).encode());
});
~~~~

The final parameter is an `ExceptionMapper` (functional Interface) which can be used to map and exception to the method fail class. I use exceptions in the service implementation sometimes when things go wrong, we can map specific exceptions to error codes. 

Javascript does not support exception mapping for implemented services.

~~~~javascript

    var service = ServiceProxy.registerService(vertx, "javascript-service", com.odobo.vertx3.serviceproxy.SampleInterface.class, {
        method1: function (str, int, obj, sh) {
            sh.ok(obj);
        },
        specialMethod1: function (str, obj, sh) {
            // Implementation
        }
    });
    
~~~~

### Creating a client

Use the createClient method of service proxy

~~~~java
       SampleInterface p = ServiceProxy.createClient(this.vertx, SOME_SERVICE_PROXY_ADDRESS, SampleInterface.class);
~~~~
~~~~javascript
       var proxy = ServiceProxy.createClient(vertx, "some-proxy-address", com.odobo.vertx3.serviceproxy.SampleInterface.class);
~~~~

## Custom serialization

Use `@ProxyObject( serializer = <Some Class>.class)` on the parameter to use custom serialization. The serialiser class must implement `ProxyJsonSerializer`.

## CompletableFuture

The class `CompletatbleFutureHandler` can be used to reduce callback hell.

~~~~java
        CompletableFutureHandler<SamplePojo> cf = new CompletableFutureHandler<>();
        p.method1("string", 3, samplePojo, cf.toHandler());

        cf.whenComplete( (value, ex) ->{
           // Handle the response
        });
~~~~

## How it works

The request is serialized to a Json specifying the method and the arguments in the java implementation this is done via an `InvocationHandler`; in javascript it's done natively e.g.

~~~~json
{
  "method": "method1",
  "arguments": [
    "string",
    3,
    {
      "string": "value",
      "intValue": 123,
      "jsonObject": {
        "v": 1
      }
    }
  ]
}
~~~~

The serivce implementat part re-constitutes the the parameters, adds a ServiceHandler to the parameters and calls the method identifier.

And the response is of the format:

~~~~json
{
	"return":{
		"string":"value",
		"intValue":123,
		"jsonObject":{"v":1}
	}
}
~~~~

When an Interface is passed to the register or create client functions, it is parsed to generate a ServiceMetaData object, this meta data is then used to create the request and responses.